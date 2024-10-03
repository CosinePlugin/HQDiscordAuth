package kr.cosine.discordauth.enums

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
        messages.forEach { message ->
            player.sendMessage(replace(message))
        }
    }

    companion object {
        fun of(text: String): Message? {
            return runCatching { valueOf(text.uppercase().replace("-", "_")) }.getOrNull()
        }
    }
}