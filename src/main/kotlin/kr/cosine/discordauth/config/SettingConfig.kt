package kr.cosine.discordauth.config

import kr.cosine.discordapi.service.BotService
import kr.cosine.discordauth.data.ChangedNickname
import kr.cosine.discordauth.data.ChangedRole
import kr.cosine.discordauth.data.LazyEmbed
import kr.cosine.discordauth.enums.AuthType
import kr.cosine.discordauth.enums.Message
import kr.cosine.discordauth.registry.SettingRegistry
import kr.hqservice.framework.bukkit.core.extension.colorize
import kr.hqservice.framework.global.core.component.Bean
import kr.hqservice.framework.yaml.config.HQYamlConfiguration
import kr.hqservice.framework.yaml.config.HQYamlConfigurationSection
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.awt.Color
import java.util.UUID
import java.util.logging.Logger

@Bean
class SettingConfig(
    private val logger: Logger,
    private val config: HQYamlConfiguration,
    private val settingRegistry: SettingRegistry,
    private val botService: BotService
) {
    fun load() {
        loadAdmins()
        loadAuthType()
        loadAllowedCommands()
        loadJoinAuthMessages()
        loadTextChannel()
        loadChangedRole()
        loadChangedNickname()
        loadAuthEmbed()
        loadLogEmbed()
        loadAuthButton()
        loadAuthModal()
        loadMessage()
        sendAuthMessage()
    }

    private fun loadAdmins() {
        val admins = config.getStringList("admins").map(UUID::fromString)
        settingRegistry.setAdmins(admins)
    }

    private fun loadAuthType() {
        val authType = config.getString("auth-type").run(AuthType::of) ?: return
        settingRegistry.setAuthType(authType)
    }

    private fun loadAllowedCommands() {
        val allowedCommands = config.getStringList("allowed-commands")
        settingRegistry.setAllowedCommands(allowedCommands)
    }

    private fun loadJoinAuthMessages() {
        val joinAuthMessage = config.getStringList("join-auth-messages").joinToString("\n").colorize()
        settingRegistry.setJoinAuthMessage(joinAuthMessage)
    }

    private fun loadTextChannel() {
        config.getSection("bot.channel-id")?.apply {
            val authChannel = findTextChannel("auth")
            if (authChannel != null) {
                settingRegistry.setAuthChannel(authChannel)
            }
            val logChannel = findTextChannel("log")
            if (logChannel != null) {
                settingRegistry.setLogChannel(logChannel)
            }
        }
    }

    private fun HQYamlConfigurationSection.findTextChannel(path: String): TextChannel? {
        val textChannelId = getLong(path)
        val textChannel = botService.findTextChannelById(textChannelId)
        if (textChannel == null) {
            logger.warning("$textChannelId 아이디를 가진 채널을 찾을 수 없습니다.")
        }
        return textChannel
    }

    private fun loadChangedRole() {
        config.getSection("bot.success-auth.changed-role")?.apply {
            val isEnabled = getBoolean("enabled")
            val role = findRole("role-id")
            if (role != null) {
                val changedRole = ChangedRole(isEnabled, role)
                settingRegistry.setChangedRole(changedRole)
            }
        }
    }

    private fun HQYamlConfigurationSection.findRole(path: String): Role? {
        val roleId = getLong(path)
        val role = botService.findRoleById(roleId)
        if (role == null) {
            logger.warning("$roleId 아이디를 가진 역할이 없습니다.")
        }
        return role
    }

    private fun loadChangedNickname() {
        config.getSection("bot.success-auth.changed-nickname")?.apply {
            val isEnabled = getBoolean("enabled")
            val nickname = getString("nickname")
            val changedNickname = ChangedNickname(isEnabled, nickname)
            settingRegistry.setChangedNickname(changedNickname)
        }
    }

    private fun loadAuthEmbed() {
        val authLazyEmbed = config.findLazyEmbed("bot.auth-embed") ?: return
        settingRegistry.setAuthLazyEmbed(authLazyEmbed)
    }

    private fun loadLogEmbed() {
        val logLazyEmbed = config.findLazyEmbed("bot.log-embed") ?: return
        settingRegistry.setLogLazyEmbed(logLazyEmbed)
    }

    private fun HQYamlConfigurationSection.findLazyEmbed(path: String): LazyEmbed? {
        return getSection(path)?.let {
            val color = it.getColor("color")
            val title = it.getString("title")
            val titleUrl = it.getString("title-url")
            val description = it.getString("description")
            val thumbnail = it.getString("thumbnail")
            val image = it.getString("image")
            val authorName = it.getString("author-name")
            val authorUrl = it.getString("author-url")
            val authorIconUrl = it.getString("author-icon-url")
            val fields = it.getStringList("fields").map { field ->
                val split = field.split(", ")
                Triple(split[0], split[1], split[2].toBoolean())
            }
            val footer = it.getString("footer")
            LazyEmbed(color, title, titleUrl, description, thumbnail, image, authorName, authorUrl, authorIconUrl, fields, footer)
        }
    }

    private fun HQYamlConfigurationSection.getColor(path: String): Color {
        val color = getString(path).toInt(16)
        return Color(color)
    }

    private fun loadAuthButton() {
        config.getSection("bot.auth-button")?.apply {
            val style = getString("style").run(ButtonStyle::valueOf)
            val label = getString("label")
            val emoji = findString("emoji")?.run(Emoji::fromUnicode)
            val authButton = Button.of(style, SettingRegistry.AUTH_BUTTON_ID, label).run {
                if (emoji != null) {
                    withEmoji(emoji)
                } else {
                    this
                }
            }
            settingRegistry.setAuthButton(authButton)
        }
    }

    private fun loadAuthModal() {
        config.getSection("bot.auth-modal")?.apply {
            val title = getString("title")
            val label = getString("label")
            val placeholder = getString("placeholder")
            val input = TextInput.create(SettingRegistry.AUTH_INPUT_ID, label, TextInputStyle.SHORT)
                .setPlaceholder(placeholder)
                .setMinLength(INPUT_LENGTH)
                .setMaxLength(INPUT_LENGTH)
                .setRequired(true)
                .build()
            val modal = Modal.create(SettingRegistry.AUTH_MODAL_ID, title)
                .addActionRow(input)
                .build()
            settingRegistry.setAuthModal(modal)
        }
    }

    private fun loadMessage() {
        config.getSection("message")?.apply {
            getKeys().forEach { messageText ->
                val message = Message.of(messageText) ?: return@forEach
                val messages = getStringList(messageText).map(String::colorize)
                message.setMessages(messages)
            }
        }
    }

    private fun sendAuthMessage() {
        val authChannel = settingRegistry.findAuthChannel() ?: return
        botService.deleteAllMessageFromTextChannel(authChannel)

        val authEmbed = settingRegistry.findAuthLazyEmbed()?.toEmbed() ?: return
        val authButton = settingRegistry.findAuthButton() ?: return
        val messageCreateData = MessageCreateBuilder().setEmbeds(authEmbed).setActionRow(authButton).build()
        authChannel.sendMessage(messageCreateData).queue()
    }

    fun reload() {
        settingRegistry.clear()
        config.reload()
        load()
    }

    private companion object {
        const val INPUT_LENGTH = 5
    }
}