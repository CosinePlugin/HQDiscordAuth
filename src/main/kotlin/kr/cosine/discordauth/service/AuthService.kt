package kr.cosine.discordauth.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.cosine.discordapi.service.BotService
import kr.cosine.discordauth.enums.AuthType
import kr.cosine.discordauth.enums.Message
import kr.cosine.discordauth.extension.textComponentArrayOf
import kr.cosine.discordauth.registry.AuthCodeRegistry
import kr.cosine.discordauth.registry.AuthorizedPlayerRegistry
import kr.cosine.discordauth.registry.SettingRegistry
import kr.cosine.discordauth.util.TimeUtils
import kr.cosine.discordauth.util.Timer
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.bukkit.core.coroutine.extension.BukkitMain
import kr.hqservice.framework.global.core.component.Service
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthService(
    private val plugin: HQBukkitPlugin,
    private val settingRegistry: SettingRegistry,
    private val authorizedPlayerRegistry: AuthorizedPlayerRegistry,
    private val authCodeRegistry: AuthCodeRegistry,
    private val botService: BotService
) {
    private val server = plugin.server

    fun startAuthByJoin(uniqueId: UUID): String? {
        if (!settingRegistry.isAuthType(AuthType.JOIN)) return null
        if (authorizedPlayerRegistry.isAuthorizedPlayer(uniqueId)) return null

        var code = authCodeRegistry.findAuthCode(uniqueId)
        if (!authCodeRegistry.isAuthPlayer(uniqueId)) {
            code = authCodeRegistry.addAuthCode(uniqueId)
        }

        Timer(
            onTimer = {
                !authCodeRegistry.isAuthPlayer(uniqueId)
            },
            onCancel = {
                authCodeRegistry.removeAuthCode(uniqueId)
            }
        ).runTaskTimer(plugin, 0, 20)

        return settingRegistry.getJoinAuthMessage().replace("%code%", code ?: "§cERROR")
    }

    fun startAuthByCommand(player: Player) {
        if (!settingRegistry.isAuthType(AuthType.COMMAND)) {
            Message.NOT_SUPPORT_AUTH_TYPE.sendMessage(player)
            return
        }
        val playerUniqueId = player.uniqueId
        if (authorizedPlayerRegistry.isAuthorizedPlayer(playerUniqueId)) {
            Message.ALREADY_AUTHORIZED.sendMessage(player)
            return
        }
        if (authCodeRegistry.isAuthPlayer(playerUniqueId)) {
            Message.ALREAY_GENERATED_AUTH_CODE.sendMessage(player)
            return
        }

        val code = authCodeRegistry.addAuthCode(playerUniqueId)
        Message.GENERATE_AUTH_CODE.sendMessage(player) {
            it.replace("%code%", code)
        }

        Timer(
            onTimer = {
                !authCodeRegistry.isAuthPlayer(playerUniqueId)
            },
            onTimeOut = {
                Message.EXPIRED_AUTH_CODE.sendMessage(player)
            },
            onCancel = {
                authCodeRegistry.removeAuthCode(playerUniqueId)
            }
        ).runTaskTimer(plugin, 0, 20)
    }

    fun submitAuthCode(member: Member, code: String, reply: (String) -> Unit) {
        val user = member.user
        val userId = user.idLong
        if (authorizedPlayerRegistry.isAuthorizedPlayer(userId)) {
            return reply("이미 연동된 계정입니다.")
        }
        if (!authCodeRegistry.isAuthCode(code)) {
            return reply("올바르지 않은 코드입니다.")
        }
        val playerUniqueId = authCodeRegistry.removeByAuthCode(code) ?: return reply("UUID를 찾지 못했습니다.")
        val offlinePlayer = server.getOfflinePlayer(playerUniqueId)
        val minecraftName = offlinePlayer.name ?: return reply("마인크래프트 닉네임을 찾지 못했습니다.")

        authorizedPlayerRegistry.setAuthorizedPlayer(playerUniqueId, userId)
        authCodeRegistry.removeAuthCode(playerUniqueId)

        val changedRole = settingRegistry.findChangedRole()
        if (changedRole != null && changedRole.isEnabled) {
            botService.addRoleToMember(member, changedRole.role).submit().join()
        }
        val changedNickname = settingRegistry.findChangedNickname()
        if (changedNickname != null && changedNickname.isEnabled) {
            val nickname = changedNickname.nickname
                .replace("%minecraft_name%", minecraftName)
            botService.modifyNickname(member, nickname).submit().join()
        }

        val player = offlinePlayer.player
        val userName = user.name
        if (player != null) {
            Message.SUCCESS_AUTH.sendMessage(player) {
                it.replace("%discord%", userName)
            }
        }
        val logChannel = settingRegistry.findLogChannel()
        val logLazyEmbed = settingRegistry.findLogLazyEmbed()
        if (logChannel != null && logLazyEmbed != null) {
            val currentTime = LocalDateTime.now()
            val userEffectiveName = user.effectiveName
            val embed = logLazyEmbed.toEmbed {
                it.replace("%player_name%", minecraftName)
                    .replace("%player_uuid%", "$playerUniqueId")
                    .replace("%discord_id_long%", "$userId")
                    .replace("%discord_id_string%", userName)
                    .replace("%discord_name%", userEffectiveName)
                    .replace("%short_time%", TimeUtils.getShortFormattedTime(currentTime))
                    .replace("%full_time%", TimeUtils.getFullFormattedTime(currentTime))
            }
            val messageCreateData = MessageCreateBuilder().setEmbeds(embed).build()
            logChannel.sendMessage(messageCreateData).queue()
        }
        reply("인증에 성공하였습니다.")
    }

    fun removeAuthorizedPlayerByDiscordId(discordId: Long) {
        val uniqueId = authorizedPlayerRegistry.findUniqueIdByDiscordId(discordId) ?: return
        authorizedPlayerRegistry.removeAuthorizedPlayer(uniqueId)
    }

    fun isAuthorizedPlayer(uniqueId: UUID): Boolean {
        return authorizedPlayerRegistry.isAuthorizedPlayer(uniqueId)
    }

    fun sendSuggestAuthMessage(player: Player) {
        val suggestAuthMessageDelay = settingRegistry.getSuggestAuthMessageDelay()
        val send = { Message.SUGGEST_AUTH.sendMessage(player) }
        if (suggestAuthMessageDelay == 0L) {
            send()
        } else {
            plugin.launch(Dispatchers.BukkitMain) {
                bukkitDelay(suggestAuthMessageDelay)
                send()
            }
        }
    }

    suspend fun removeAuth(name: String): Boolean {
        return withContext(Dispatchers.IO) {
            val targetOfflinePlayer = server.getOfflinePlayer(name)
            val targetUniqueId = targetOfflinePlayer.uniqueId
            if (!isAuthorizedPlayer(targetUniqueId)) {
                return@withContext false
            }
            val targetDiscordId = authorizedPlayerRegistry.getDiscordId(targetUniqueId)
            val targetMember = botService.findMemberById(targetDiscordId)
            val changedRole = settingRegistry.findChangedRole()
            if (targetMember != null && changedRole != null) {
                botService.removeRoleToMember(targetMember, changedRole.role).queue()
            }
            authorizedPlayerRegistry.removeAuthorizedPlayer(targetUniqueId)
            return@withContext true
        }
    }

    fun showDiscordNicknameAndIdByMinecraftUniqueIdOrName(sender: CommandSender, uniqueIdOrName: String) {
        plugin.launch(Dispatchers.IO) {
            val target = runCatching {
                val targetUniqueId = UUID.fromString(uniqueIdOrName)
                server.getOfflinePlayer(targetUniqueId)
            }.getOrNull() ?: server.getOfflinePlayer(uniqueIdOrName)
            val targetUniqueId = target.uniqueId
            val targetDiscordId = authorizedPlayerRegistry.findDiscordId(targetUniqueId) ?: run {
                sender.sendMessage("§c인증되지 않은 유저입니다.")
                return@launch
            }
            val targetMember = botService.findMemberById(targetDiscordId) ?: run {
                sender.sendMessage("§c디스코드 방에 없는 유저입니다.")
                return@launch
            }
            sender.sendMessage("§6${target.name}님의 디스코드 정보")
            sender.spigot().sendMessage(createClipboardTextComponent("UUID", "§7(${targetUniqueId})", "$targetUniqueId"))
            sender.sendInfoMessage("문자 아이디", targetMember.user.name)
            sender.sendInfoMessage("숫자 아이디", "${targetMember.idLong}")
            sender.sendInfoMessage("원본 닉네임", targetMember.user.effectiveName)
            sender.sendInfoMessage("변경된 닉네임", targetMember.effectiveName)
        }
    }

    fun showMinecraftNameAndUniqueIdByDiscordId(sender: CommandSender, discordId: Long) {
        plugin.launch(Dispatchers.IO) {
            val targetUniqueId = authorizedPlayerRegistry.findUniqueIdByDiscordId(discordId) ?: run {
                sender.sendMessage("§c인증하지 않은 유저입니다.")
                return@launch
            }
            val target = server.getOfflinePlayer(targetUniqueId)
            val targetMember = botService.findMemberById(discordId)
            sender.sendMessage("§6${targetMember?.user?.name ?: "§c불러오지 못함"}§6님의 마인크래프트 정보")
            sender.spigot().sendMessage(createClipboardTextComponent("디스코드 아이디", "§7(${discordId})", "$discordId"))
            sender.sendInfoMessage("마크 닉네임", "${target.name}")
            sender.sendInfoMessage("마크 UUID", "$targetUniqueId")
        }
    }

    private fun CommandSender.sendInfoMessage(title: String, text: String) {
        val textComponent = TextComponent().apply {
            addExtra(TextComponent("§f└ $title: "))
            addExtra(createClipboardTextComponent(title, text))
        }
        spigot().sendMessage(textComponent)
    }

    private fun createClipboardTextComponent(title: String, text: String, copy: String = text): TextComponent {
        return TextComponent("§a$text").apply {
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, textComponentArrayOf("§f클릭 시 ${title}을(를) 복사합니다."))
            clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copy)
        }
    }
}