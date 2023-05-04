package kr.hqservice.auth.extension

import org.bukkit.ChatColor
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection




internal fun String.isInt(): Boolean {
    return try {
        this.toInt()
        true
    } catch (e: NumberFormatException) {
        false
    }
}

internal fun String.applyColor(): String {
    return ChatColor.translateAlternateColorCodes('&', this)
}

internal fun List<String>.applyColor(): MutableList<String> {
    return map { it.applyColor() }.toMutableList()
}

internal fun List<String>.applyText(from: String, to: String): MutableList<String> {
    return map { it.replace(from, to) }.toMutableList()
}