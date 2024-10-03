package kr.cosine.discordauth.command.provider

import kr.cosine.discordauth.command.argument.NameArgument
import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.bukkit.Server

@Component
class NameArgumentProvider(
    private val server: Server
) : CommandArgumentProvider<NameArgument> {

    override suspend fun cast(context: CommandContext, argument: String?): NameArgument {
        if (argument == null) {
            throw ArgumentFeedback.Message("§c닉네임을 입력해주세요.")
        }
        return NameArgument(argument)
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return server.onlinePlayers.map { it.name }
    }
}