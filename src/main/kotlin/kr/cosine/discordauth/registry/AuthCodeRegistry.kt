package kr.cosine.discordauth.registry

import kr.cosine.discordauth.util.RandomCode
import kr.hqservice.framework.global.core.component.Bean
import java.util.UUID

@Bean
class AuthCodeRegistry {
    private val randomCode = RandomCode()

    private val authCodeMap = mutableMapOf<UUID, String>()

    fun isAuthPlayer(uniqueId: UUID): Boolean {
        return authCodeMap.containsKey(uniqueId)
    }

    fun isAuthCode(code: String): Boolean {
        return authCodeMap.containsValue(code)
    }

    fun findAuthCode(uniqueId: UUID): String? {
        return authCodeMap[uniqueId]
    }

    fun addAuthCode(uniqueId: UUID): String {
        val code = randomCode.generate()
        authCodeMap[uniqueId] = code
        return code
    }

    fun removeAuthCode(uniqueId: UUID) {
        authCodeMap.remove(uniqueId)
    }

    fun removeByAuthCode(code: String): UUID? {
        val uniqueId = authCodeMap.entries.find {
            it.value == code
        }?.key ?: return null
        authCodeMap.remove(uniqueId)
        return uniqueId
    }
}