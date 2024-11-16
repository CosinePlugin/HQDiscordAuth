package kr.cosine.discordauth.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.cosine.discordauth.command.argument.NameArgument
import kr.cosine.discordauth.config.AuthorizedPlayerConfig
import kr.cosine.discordauth.config.SettingConfig
import kr.cosine.discordauth.service.AuthService
import kr.hqservice.framework.command.ArgumentLabel
import kr.hqservice.framework.command.Command
import kr.hqservice.framework.command.CommandExecutor
import org.bukkit.command.CommandSender

@Command(label = "인증관리", isOp = true)
class AuthAdminCommand(
    private val settingConfig: SettingConfig,
    private val authorizedPlayerConfig: AuthorizedPlayerConfig,
    private val authService: AuthService
) {
    @CommandExecutor("제거", "해당 유저의 인증을 제거합니다.", priority = 1)
    suspend fun removeAuth(
        sender: CommandSender,
        @ArgumentLabel("유저") nameArgument: NameArgument
    ) {
        val name = nameArgument.name
        if (authService.removeAuth(name)) {
            sender.sendMessage("§a${name}님의 인증 정보가 제거되었습니다.")
        } else {
            sender.sendMessage("§c인증을 하지 않은 유저입니다.")
        }
    }

    @CommandExecutor("저장", "변경된 사항을 수동으로 저장합니다.", priority = 2)
    suspend fun save(sender: CommandSender) {
        withContext(Dispatchers.IO) {
            authorizedPlayerConfig.save()
            sender.sendMessage("§a변경된 사항을 수동으로 저장하였습니다.")
        }
    }

    @CommandExecutor("리로드", "config.yml을 리로드합니다.", priority = 3)
    fun reload(sender: CommandSender) {
        settingConfig.reload()
        sender.sendMessage("§aconfig.yml을 리로드하였습니다.")
    }
}