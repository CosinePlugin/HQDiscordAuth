package kr.hqservice.auth.repository

import kr.hqservice.auth.repository.data.AuthBotData
import kotlin.reflect.KClass

interface Registrable<T : AuthBotData> {

    fun register(vararg kClasses: KClass<out T>)

    fun unregister(vararg kClasses: KClass<out T>)

    fun isRegistered(vararg kClasses: KClass<out T>): Boolean

    @Throws(IllegalArgumentException::class)
    fun <K : T> get(clazz: KClass<out K>) : K
}