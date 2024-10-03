package kr.cosine.discordauth.enums

enum class AuthType {
    JOIN,
    COMMAND;

    companion object {
        fun of(text: String): AuthType? {
            return runCatching { valueOf(text.uppercase()) }.getOrNull()
        }
    }
}