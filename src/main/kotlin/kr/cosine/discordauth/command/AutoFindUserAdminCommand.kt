package kr.cosine.discordauth.command

import kr.cosine.discordauth.command.argument.DiscordIdArgument
import kr.cosine.discordauth.command.argument.UniqueIdOrNameArgument
import kr.cosine.discordauth.service.AuthService
import kr.hqservice.framework.command.ArgumentLabel
import kr.hqservice.framework.command.Command
import kr.hqservice.framework.command.CommandExecutor
import org.bukkit.command.CommandSender

@Command(parent = AuthAdminCommand::class, label = "유저조회")
class AutoFindUserAdminCommand(
    private val authService: AuthService
) {
    @CommandExecutor("마크", "마크 UUID/닉네임을 기반으로 디코 닉네임/아이디를 조회합니다.", priority = 1)
    fun showDiscordNameAndId(
        sender: CommandSender,
        @ArgumentLabel("마크 UUID/닉네임") uniqueIdOrNameArgument: UniqueIdOrNameArgument
    ) {
        authService.showDiscordNicknameAndIdByMinecraftUniqueIdOrName(sender, uniqueIdOrNameArgument.uniqueIdOrName)
    }

    @CommandExecutor("디코", "디코 아이디를 기반으로 마크 닉네임/UUID를 조회합니다.", priority = 2)
    fun showMinecraftNameAndUniqueId(
        sender: CommandSender,
        @ArgumentLabel("디코 아이디") discordIdArgument: DiscordIdArgument
    ) {
        authService.showMinecraftNameAndUniqueIdByDiscordId(sender, discordIdArgument.discordId)
    }
}