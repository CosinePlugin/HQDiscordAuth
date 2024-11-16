package kr.cosine.discordauth.command.provider

import kr.cosine.discordauth.command.argument.NameArgument
import kr.cosine.discordauth.command.argument.UniqueIdOrNameArgument
import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location
import org.bukkit.Server

@Component
class UniqueIdOrNameArgumentProvider(
    private val server: Server
) : CommandArgumentProvider<UniqueIdOrNameArgument> {
    override suspend fun cast(context: CommandContext, argument: String?): UniqueIdOrNameArgument {
        if (argument == null) {
            throw ArgumentFeedback.Message("§cUUID 또는 닉네임을 입력해주세요.")
        }
        return UniqueIdOrNameArgument(argument)
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        val onlinePlayers = server.onlinePlayers
        return onlinePlayers.map { it.name } + onlinePlayers.map { "${it.uniqueId}" }
    }
}