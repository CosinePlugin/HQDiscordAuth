package kr.cosine.discordauth.enums

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.entity.Player

enum class Message(
    private var messages: List<String>
) {
    SUGGEST_AUTH(listOf("§6[인증] §f'/인증' 명령어를 통해 인증을 진행해주세요.")),
    NOT_SUPPORT_AUTH_TYPE(listOf("§6[인증] §f현재 해당 인증 방식을 사용할 수 없습니다.")),
    ALREADY_AUTHORIZED(listOf("§6[인증] §f이미 인증된 상태입니다.")),
    ALREAY_GENERATED_AUTH_CODE(listOf("§6[인증] §f이미 인증 코드가 발급된 상태입니다.")),
    GENERATE_AUTH_CODE(
        listOf(
            "",
            "§6[인증] §f인증 코드가 발급되었습니다. 디스코드 인증 채널에서 인증해주세요.",
            "§7└ 인증 코드: §a%code%",
            "§7└ 30초 후에 인증 코드가 자동으로 §c만료§7됩니다.",
            ""
        )
    ),
    EXPIRED_AUTH_CODE(listOf("§6[인증] §f인증 코드가 만료되었습니다.")),
    SUCCESS_AUTH(listOf("§6[인증] §f인증에 성공하여 %discord% 계정과 연동되었습니다.")),
    BLOCKED_COMMAND_BEFORE_AUTH(listOf("§6[인증] §f인증 전에는 명령어를 사용할 수 없습니다."));

    fun setMessages(messages: List<String>) {
        this.messages = messages
    }

    fun sendMessage(player: Player, replace: (String) -> String = { it }) {
        if (messages.isEmpty()) return
        val textComponent = messages.joinToString("\n", transform = replace)
            .run(::createLinkedTextComponent)
        player.spigot().sendMessage(textComponent)
    }

    companion object {
        fun of(text: String): Message? {
            return runCatching { valueOf(text.uppercase().replace("-", "_")) }.getOrNull()
        }

        private val linkedMessageRegex = Regex("`\\[(.*?)](<(.*?)>)?\\((.*?)\\)`")

        private fun createLinkedTextComponent(text: String): TextComponent {
            val matches = linkedMessageRegex.findAll(text)

            val component = TextComponent()
            var currentIndex = 0

            for (match in matches) {
                val (messageText, _, hoverText, link) = match.destructured
                val textBeforeLink = text.substring(currentIndex, match.range.first)
                if (textBeforeLink.isNotBlank()) {
                    component.addExtra(textBeforeLink)
                }
                val messageComponent = TextComponent(messageText)
                if (hoverText.isNotEmpty()) {
                    val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(hoverText))
                    messageComponent.hoverEvent = hoverEvent
                }
                messageComponent.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, link)
                component.addExtra(messageComponent)
                currentIndex = match.range.last + 1
            }

            val remainingText = text.substring(currentIndex)
            if (remainingText.isNotBlank()) {
                component.addExtra(remainingText)
            }
            return component
        }
    }
}