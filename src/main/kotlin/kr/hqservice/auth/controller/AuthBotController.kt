package kr.hqservice.auth.controller

import kr.hqservice.auth.HQDiscordAuth
import kr.hqservice.auth.builder.AuthButtonBuilder
import kr.hqservice.auth.builder.AuthEmbedBuilder
import kr.hqservice.auth.listener.DiscordListener
import kr.hqservice.auth.repository.data.impl.AuthBotMessage
import kr.hqservice.auth.repository.data.impl.AuthBotSetting
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

class AuthBotController(
    private val plugin: HQDiscordAuth
) {

    companion object {
        const val buttonId = "HQDiscordAuth-Button"
    }

    private val authConfigRepository = plugin.authConfigRepository

    private val authBotMessage = authConfigRepository.get(AuthBotMessage::class)
    private val authBotSetting = authConfigRepository.get(AuthBotSetting::class)

    lateinit var jda: JDA
        private set

    lateinit var guild: Guild
        private set

    lateinit var authChannel: TextChannel
        private set

    var logChannel: TextChannel? = null
        private set

    fun load() {
        val builder = JDABuilder.createDefault(authBotSetting.token, GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))

        val botInfo = authBotSetting.botInfo
        builder.setStatus(botInfo.status)
        builder.setActivity(Activity.playing(botInfo.statusMessage))
        builder.addEventListeners(DiscordListener(plugin))

        jda = builder.build()
        jda.awaitReady()

        setDefault()
    }

    private fun setDefault() {
        val discordId = authBotSetting.discordId

        val guildId = discordId.guildId
        guild = jda.getGuildById(guildId) ?: run {
            plugin.logger.warning("${guildId}의 아이디를 가진 디스코드 방을 찾을 수 없습니다.")
            return
        }

        val authId = discordId.authId
        authChannel = jda.getTextChannelById(authId) ?: run {
            plugin.logger.warning("${authId}의 아이디를 가진 디스코드 채널을 찾을 수 없습니다.")
            return
        }

        if (authBotSetting.logEnable) {
            discordId.logId?.let {
                logChannel = jda.getTextChannelById(it) ?: run {
                    plugin.logger.warning("로그가 활성화 되어 있지만, ${authId}의 아이디를 가진 디스코드 채널을 찾을 수 없습니다.")
                    return
                }
            }
        }

        authChannel.deleteAuthMessage()
        authChannel.sendAuthMessage()
    }

    private fun TextChannel.sendAuthMessage() {
        val embed = AuthEmbedBuilder(authBotMessage).setDeafult().build()
        val button = AuthButtonBuilder(authBotMessage).build()
        val message = MessageCreateBuilder().setEmbeds(embed).setActionRow(ActionRow.of(button).components).build()

        sendMessage(message).queue()
    }

    private fun TextChannel.deleteAuthMessage() {
        val history = MessageHistory.getHistoryFromBeginning(this).complete()
        val messages = history.retrievedHistory.filter {
            it.buttons.isNotEmpty() && it.buttons.first().id == buttonId
        }
        for (message in messages) {
            message.delete().queue()
        }
    }

    fun unload() {
        jda.shutdown()
        //jda.awaitShutdown(Duration.ofMillis(500))
    }
}