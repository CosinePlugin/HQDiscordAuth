package kr.cosine.discordauth.command.provider

import kr.cosine.discordauth.command.argument.DiscordIdArgument
import kr.cosine.discordauth.registry.AuthorizedPlayerRegistry
import kr.hqservice.framework.command.CommandArgumentProvider
import kr.hqservice.framework.command.CommandContext
import kr.hqservice.framework.command.argument.exception.ArgumentFeedback
import kr.hqservice.framework.global.core.component.Component
import org.bukkit.Location

@Component
class DiscordIdArgumentProvider(
    private val authorizedPlayerRegistry: AuthorizedPlayerRegistry
) : CommandArgumentProvider<DiscordIdArgument> {
    override suspend fun cast(context: CommandContext, argument: String?): DiscordIdArgument {
        if (argument == null) {
            throw ArgumentFeedback.Message("§c디스코드 아이디를 입력해주세요.")
        }
        val discordId = argument.toLongOrNull() ?: throw ArgumentFeedback.Message("§c숫자만 입력할 수 있습니다.")
        return DiscordIdArgument(discordId)
    }

    override suspend fun getTabComplete(context: CommandContext, location: Location?): List<String> {
        return authorizedPlayerRegistry.getDiscordIds().map(Long::toString)
    }
}