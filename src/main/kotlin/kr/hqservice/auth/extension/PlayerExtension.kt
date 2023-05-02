package kr.hqservice.auth.extension

import org.bukkit.entity.Player

internal fun Player.sendMessages(messages: List<String>) {
    messages.forEach { sendMessage(it) }
}