package kr.cosine.discordauth.command.module

import kr.cosine.discordauth.command.AuthUserCommand
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup

@Module
class CommandModule(
    private val plugin: HQBukkitPlugin,
    private val authUserCommand: AuthUserCommand
) {
    @Setup
    fun setup() {
        plugin.getCommand("인증")?.setExecutor(authUserCommand)
    }
}