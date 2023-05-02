package kr.hqservice.auth.command

import kr.hqservice.auth.HQDiscordAuth
import kr.hqservice.auth.extension.*
import kr.hqservice.auth.repository.data.impl.AuthBotSetting
import kr.hqservice.auth.repository.data.impl.AuthMessage
import kr.hqservice.auth.repository.data.impl.AuthMessage.Companion.prefix
import kr.hqservice.auth.util.Time
import kr.hqservice.auth.util.Time.toText
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class AuthAdminCommand(
    private val plugin: HQDiscordAuth
) : CommandExecutor {

    private val authConfigRepository = plugin.authConfigRepository
    private val authRepository = plugin.authRepository
    private val authBotController = plugin.authBotController

    private val authBotSetting = authConfigRepository.get(AuthBotSetting::class)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("$prefix /인증제거 [닉네임]")
            return true
        }
        async {
            val target = plugin.server.getOfflinePlayer(args[0]) ?: run {
                sender.sendMessage("$prefix 존재하지 않는 플레이어입니다.")
                return@async
            }
            val targetUUID = target.uniqueId
            if (!authRepository.containsKey(targetUUID)) {
                sender.sendMessage("$prefix 인증을 하지 않은 유저입니다.")
                return@async
            }
            val targetDiscordId = authRepository.get(targetUUID)

            if (targetDiscordId != null) {
                authBotController.guild.removeRolesFromMember(targetDiscordId, authBotSetting.changedRole.roles)
            }

            authRepository.remove(targetUUID)
            sender.sendMessage("$prefix ${target.name}님의 인증 정보가 제거되었습니다.")
        }
        return true
    }

    private fun Guild.removeRolesFromMember(discordId: Long, roles: Collection<Long>) {
        async {
            retrieveMemberById(discordId).queue { member ->
                roles.forEach {
                    val role = getRoleById(it) ?: return@forEach

                    removeRoleFromMember(member, role).queue()
                    later(10) { modifyNickname(member, member.user.name).queue() }
                }
            }
        }
    }
}