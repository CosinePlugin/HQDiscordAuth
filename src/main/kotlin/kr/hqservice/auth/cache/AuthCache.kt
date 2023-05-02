package kr.hqservice.auth.cache

import kr.hqservice.auth.util.RandomCode
import java.util.*

class AuthCache {

    private val random = RandomCode()

    private val auths = mutableMapOf<UUID, Int>()

    fun containsKey(uuid: UUID): Boolean {
        return auths.containsKey(uuid)
    }

    fun containsValue(code: Int): Boolean {
        return auths.containsValue(code)
    }

    fun get(uuid: UUID): Int? {
        return auths[uuid]
    }

    fun add(uuid: UUID) {
        val code = random.generate()
        if (auths.values.contains(code)) {
            add(uuid)
            return
        }
        auths[uuid] = random.generate()
    }

    fun remove(uuid: UUID) {
        auths.remove(uuid)
    }

    fun removeByCode(code: Int): UUID {
        val uuid = auths.filter { it.value == code }.keys.first()
        auths.remove(uuid)
        return uuid
    }
}