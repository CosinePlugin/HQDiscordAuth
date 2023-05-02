package kr.hqservice.auth.command

import kr.hqservice.auth.HQDiscordAuth
import kr.hqservice.auth.extension.applyText
import kr.hqservice.auth.extension.async
import kr.hqservice.auth.extension.copyToClipboard
import kr.hqservice.auth.extension.sendMessages
import kr.hqservice.auth.repository.data.impl.AuthBotSetting
import kr.hqservice.auth.repository.data.impl.AuthMessage
import kr.hqservice.auth.repository.data.impl.AuthMessage.Companion.prefix
import kr.hqservice.auth.util.Time
import kr.hqservice.auth.util.Time.toText
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class AuthCommand(
    private val plugin: HQDiscordAuth
) : CommandExecutor {

    private val authCache = plugin.authCache
    private val authConfigRepository = plugin.authConfigRepository
    private val authRepository = plugin.authRepository
    private val authBotController = plugin.authBotController
    private val authMessage = authConfigRepository.get(AuthMessage::class)
    private val authBotSetting = authConfigRepository.get(AuthBotSetting::class)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("$prefix 콘솔에서 실행할 수 없는 명령어입니다.")
            return true
        }
        val uuid = sender.uniqueId
        if (authCache.containsKey(uuid)) {
            sender.sendMessages(authMessage.processAuthorizing)
            return true
        }
        if (authRepository.containsKey(uuid)) {
            sender.sendMessages(authMessage.authorized)
            return true
        }
        authCache.add(uuid)

        val code = authCache.get(uuid).toString()
        code.copyToClipboard()

        async {
            if (authBotSetting.logEnable) {
                authBotController.logChannel?.sendMessage(
                    "`[${Time.getNowTime().toText()}] ${sender.name}(${uuid})님이 인증 코드를 발급받았습니다. (인증 코드: $code)`"
                )?.queue()
            }
        }

        sender.sendMessages(authMessage.start.applyText("%code%", code))

        var time = 30
        object : BukkitRunnable() {
            override fun run() {
                time--
                if (time <= 0) {
                    cancel()
                    sender.sendMessages(authMessage.expiration)
                }
                if (!authCache.containsKey(uuid)) {
                    cancel()
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20)

        return true
    }
}