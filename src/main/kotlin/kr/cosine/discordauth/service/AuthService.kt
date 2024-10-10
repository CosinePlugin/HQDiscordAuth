package kr.cosine.discordauth.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.cosine.discordapi.service.BotService
import kr.cosine.discordauth.enums.AuthType
import kr.cosine.discordauth.enums.Message
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
}