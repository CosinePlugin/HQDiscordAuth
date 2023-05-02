package kr.hqservice.auth.listener

import kr.hqservice.auth.HQDiscordAuth
import kr.hqservice.auth.builder.AuthModalBuilder
import kr.hqservice.auth.controller.AuthBotController
import kr.hqservice.auth.extension.applyText
import kr.hqservice.auth.extension.isInt
import kr.hqservice.auth.extension.sendMessages
import kr.hqservice.auth.repository.data.impl.AuthBotSetting
import kr.hqservice.auth.repository.data.impl.AuthMessage
import kr.hqservice.auth.util.Time
import kr.hqservice.auth.util.Time.toText
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

class DiscordListener(
    private val plugin: HQDiscordAuth
) : ListenerAdapter() {

    companion object {
        const val codeTypingId = "HQDiscordAuth-Typing"
        const val modalId = "HQDiscordAuth-Modal"

        val authModalBuilder = AuthModalBuilder().build()
    }

    private val server = plugin.server

    private val authCache = plugin.authCache
    private val authConfigRepository = plugin.authConfigRepository
    private val authRepository = plugin.authRepository
    private val authBotController = plugin.authBotController
    private val authMessage = authConfigRepository.get(AuthMessage::class)
    private val authBotSetting = authConfigRepository.get(AuthBotSetting::class)

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.button.id != AuthBotController.buttonId) return

        event.replyModal(authModalBuilder).queue()
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId != modalId) return

        val codeText = event.getValue(codeTypingId)?.asString ?: return
        if (!codeText.isInt()) {
            event.reply("코드에는 숫자만 입력 가능합니다.").setEphemeral(true).queue()
            return
        }
        val code = codeText.toInt()
        if (authCache.containsValue(code)) {
            val guild = event.guild ?: return
            val member = event.member ?: return

            val uuid = authCache.removeByCode(code)
            val player = server.getPlayer(uuid) ?: return

            try {
                authRepository.add(player.uniqueId)
                val changedName = authBotSetting.changedName
                if (changedName.enable) {
                    guild.modifyNickname(
                        member,
                        changedName.name.replace("%minecraft_name%", player.name)
                    ).queue()
                }
                val changedRole = authBotSetting.changedRole
                if (changedRole.enable) {
                    guild.addRolesToMember(member, changedRole.roles)
                }

                val user = event.user.asTag
                player.sendMessages(authMessage.success.applyText("%discord%", user))

                if (authBotSetting.logEnable) {
                    authBotController.logChannel?.sendMessage(
                        "`[${Time.getNowTime().toText()}] ${player.name}(${uuid})님이 $user 디스코드 계정과의 연동을 성공하였습니다.`"
                    )?.queue()
                }

                event.reply("인증을 성공하였습니다.").setEphemeral(true).queue()
            } catch (e: Exception) {
                e.printStackTrace()
                authRepository.remove(player.uniqueId)

                player.sendMessages(authMessage.fail)

                if (authBotSetting.logEnable) {
                    authBotController.logChannel?.sendMessage(
                        "`[${Time.getNowTime().toText()}] ${player.name}(${uuid})님이 인증에 실패하였습니다.`"
                    )?.queue()
                }

                event.reply("인증에 실패하였습니다.").setEphemeral(true).queue()
            }
        } else {
            event.reply("올바르지 않은 코드를 입력하였습니다.").setEphemeral(true).queue()
        }
    }

    private fun Guild.addRolesToMember(member: Member, roles: Collection<Long>) {
        roles.forEach {
            val role = getRoleById(it) ?: return@forEach
            addRoleToMember(member, role).queue()
        }
    }
}