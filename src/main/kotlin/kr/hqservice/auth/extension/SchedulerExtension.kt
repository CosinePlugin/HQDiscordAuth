package kr.hqservice.auth.extension

import kr.hqservice.auth.HQDiscordAuth.Companion.plugin
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask

fun sync(block: () -> Unit) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable(block))
}

fun async(block: () -> Unit) {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable(block))
}

fun later(delay: Int = 1, async: Boolean = false, block: () -> Unit = {}): BukkitTask {
    return if (async) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable(block), delay.toLong())
    } else {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable(block), delay.toLong())
    }
}