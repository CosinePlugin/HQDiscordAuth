package kr.hqservice.auth.extension

import kotlin.reflect.KClass

fun <R : Any> Iterable<*>.filterKClass(klass: KClass<R>): List<R> {
    return filterIsInstanceTo(ArrayList(), klass.java)
}