package kr.cosine.discordauth.registry

import kr.hqservice.framework.global.core.component.Bean
import java.util.UUID

@Bean
class AuthorizedPlayerRegistry {
    private val authorizedPlayerMap = mutableMapOf<UUID, Long>()

    var isChanged = false

    fun restore(authorizedPlayerRegistry: AuthorizedPlayerRegistry) {
        authorizedPlayerMap.clear()
        authorizedPlayerMap.putAll(authorizedPlayerRegistry.authorizedPlayerMap)
    }

    fun isAuthorizedPlayer(uniqueId: UUID): Boolean {
        return authorizedPlayerMap.containsKey(uniqueId)
    }

    fun isAuthorizedPlayer(discordId: Long): Boolean {
        return authorizedPlayerMap.containsValue(discordId)
    }

    fun findDiscordId(uniqueId: UUID): Long? {
        return authorizedPlayerMap[uniqueId]
    }

    fun getDiscordId(uniqueId: UUID): Long {
        return findDiscordId(uniqueId)
            ?: throw IllegalArgumentException("$uniqueId(으)로부터 디스코드 아이디를 찾지 못했습니다.")
    }

    fun findUniqueIdByDiscordId(discordId: Long): UUID? {
        return authorizedPlayerMap.entries.find { it.value == discordId }?.key
    }

    fun setAuthorizedPlayer(uniqueId: UUID, discordId: Long) {
        authorizedPlayerMap[uniqueId] = discordId
        isChanged = true
    }

    fun removeAuthorizedPlayer(uniqueId: UUID) {
        authorizedPlayerMap.remove(uniqueId)
        isChanged = true
    }

    fun getDiscordIds(): List<Long> {
        return authorizedPlayerMap.values.toList()
    }
}