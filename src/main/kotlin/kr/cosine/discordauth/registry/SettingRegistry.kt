package kr.cosine.discordauth.registry

import kr.cosine.discordauth.data.ChangedNickname
import kr.cosine.discordauth.data.ChangedRole
import kr.cosine.discordauth.data.LazyEmbed
import kr.cosine.discordauth.enums.AuthType
import kr.hqservice.framework.global.core.component.Bean
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.modals.Modal
import java.util.UUID

@Bean
class SettingRegistry {
    private var admins = listOf<UUID>()

    private var authType = AuthType.COMMAND

    private var allowedCommands = listOf<String>()

    private var joinAuthMessage = ""

    private var authChannel: TextChannel? = null

    private var logChannel: TextChannel? = null

    private var authLazyEmbed: LazyEmbed? = null

    private var logLazyEmbed: LazyEmbed? = null

    private var authButton: Button? = null

    private var authModal: Modal? = null

    private var changedRole: ChangedRole? = null

    private var changedNickname: ChangedNickname? = null

    fun isAdmin(uniqueId: UUID): Boolean {
        return admins.contains(uniqueId)
    }

    fun setAdmins(admins: List<UUID>) {
        this.admins = admins
    }

    fun isAuthType(authType: AuthType): Boolean {
        return this.authType == authType
    }

    fun setAuthType(authType: AuthType) {
        this.authType = authType
    }

    fun isAllowedCommand(command: String): Boolean {
        return allowedCommands.contains(command)
    }

    fun setAllowedCommands(allowedCommands: List<String>) {
        this.allowedCommands = allowedCommands
    }

    fun getJoinAuthMessage(): String {
        return joinAuthMessage
    }

    fun setJoinAuthMessage(joinAuthMessage: String) {
        this.joinAuthMessage = joinAuthMessage
    }

    fun findAuthChannel(): TextChannel? {
        return authChannel
    }

    fun setAuthChannel(authChannel: TextChannel) {
        this.authChannel = authChannel
    }

    fun findLogChannel(): TextChannel? {
        return logChannel
    }

    fun setLogChannel(logChannel: TextChannel) {
        this.logChannel = logChannel
    }

    fun findAuthLazyEmbed(): LazyEmbed? {
        return authLazyEmbed
    }

    fun setAuthLazyEmbed(authLazyEmbed: LazyEmbed) {
        this.authLazyEmbed = authLazyEmbed
    }

    fun findLogLazyEmbed(): LazyEmbed? {
        return logLazyEmbed
    }

    fun setLogLazyEmbed(logLazyEmbed: LazyEmbed) {
        this.logLazyEmbed = logLazyEmbed
    }

    fun findAuthButton(): Button? {
        return authButton
    }

    fun setAuthButton(authButton: Button) {
        this.authButton = authButton
    }

    fun findAuthModal(): Modal? {
        return authModal
    }

    fun setAuthModal(authModal: Modal) {
        this.authModal = authModal
    }

    fun findChangedRole(): ChangedRole? {
        return changedRole
    }

    fun setChangedRole(changedRole: ChangedRole) {
        this.changedRole = changedRole
    }

    fun findChangedNickname(): ChangedNickname? {
        return changedNickname
    }

    fun setChangedNickname(changedNickname: ChangedNickname) {
        this.changedNickname = changedNickname
    }

    fun clear() {
        admins = emptyList()
        authType = AuthType.COMMAND
        allowedCommands = emptyList()
        joinAuthMessage = ""
        authChannel = null
        logChannel = null
        authLazyEmbed = null
        logLazyEmbed = null
        authButton = null
        authModal = null
        changedRole = null
        changedNickname = null
    }

    companion object {
        const val AUTH_BUTTON_ID = "HQDiscordAuth-Button"
        const val AUTH_MODAL_ID = "HQDiscordAuth-Modal"
        const val AUTH_INPUT_ID = "HQDiscordAuth-Input"
    }
}