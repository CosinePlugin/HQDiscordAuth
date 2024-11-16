package kr.cosine.discordauth.extension

import net.md_5.bungee.api.chat.TextComponent

fun textComponentListOf(vararg messages: String): List<TextComponent> {
    return messages.map(::TextComponent)
}

fun textComponentArrayOf(vararg messages: String): Array<TextComponent> {
    return arrayOf(*messages.map(::TextComponent).toTypedArray())
}