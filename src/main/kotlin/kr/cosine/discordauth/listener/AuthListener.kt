package kr.cosine.discordauth.listener

import kr.cosine.discordapi.event.AsyncDiscordButtonInteractionEvent
import kr.cosine.discordapi.event.AsyncDiscordGuildMemberRemoveEvent
import kr.cosine.discordapi.event.AsyncDiscordModalInteractionEvent
import kr.cosine.discordauth.enums.AuthType
import kr.cosine.discordauth.enums.Message
import kr.cosine.discordauth.registry.SettingRegistry
import kr.cosine.discordauth.service.AuthService
import kr.hqservice.framework.bukkit.core.listener.Listener
import kr.hqservice.framework.bukkit.core.listener.Subscribe
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent

@Listener
class AuthListener(
    private val settingRegistry: SettingRegistry,
    private val authService: AuthService
) {
    @Subscribe
    fun onAsyncDiscordButtonInteraction(event: AsyncDiscordButtonInteractionEvent) {
        if (event.button.id != SettingRegistry.AUTH_BUTTON_ID) return
        val authModal = settingRegistry.findAuthModal() ?: return
        event.replyModal(authModal)
    }

    @Subscribe
    fun onAsyncDiscordModalInteraction(event: AsyncDiscordModalInteractionEvent) {
        if (event.modalId != SettingRegistry.AUTH_MODAL_ID) return
        val code = event.findInput(SettingRegistry.AUTH_INPUT_ID)?.asString ?: return
        authService.submitAuthCode(event.member, code, event::reply)
    }

    @Subscribe
    fun onAsyncDiscordGuildMemberRemove(event: AsyncDiscordGuildMemberRemoveEvent) {
        authService.removeAuthorizedPlayerByDiscordId(event.member.idLong)
    }

    @Subscribe
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val uniqueId = event.uniqueId
        if (settingRegistry.isAdmin(uniqueId)) return
        val disallowMessage = authService.startAuthByJoin(uniqueId) ?: return
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, disallowMessage)
    }

    @Subscribe
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!settingRegistry.isAuthType(AuthType.COMMAND)) return
        val player = event.player
        val playerUniqueId = player.uniqueId
        if (settingRegistry.isAdmin(playerUniqueId)) return
        if (!authService.isAuthorizedPlayer(playerUniqueId)) {
            Message.SUGGEST_AUTH.sendMessage(player)
        }
    }

    @Subscribe
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        if (!settingRegistry.isAuthType(AuthType.COMMAND)) return
        val player = event.player
        val playerUniqueId = player.uniqueId
        if (settingRegistry.isAdmin(playerUniqueId)) return
        val rootCommand = event.message.split(" ")[0]
        if (!authService.isAuthorizedPlayer(playerUniqueId) && !settingRegistry.isAllowedCommand(rootCommand)) {
            event.isCancelled = true
            Message.BLOCKED_COMMAND_BEFORE_AUTH.sendMessage(player)
        }
    }

    @Subscribe
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!settingRegistry.isAuthType(AuthType.COMMAND)) return
        val playerUniqueId = event.player.uniqueId
        if (settingRegistry.isAdmin(playerUniqueId)) return
        if (!authService.isAuthorizedPlayer(playerUniqueId)) {
            event.isCancelled = true
        }
    }
}