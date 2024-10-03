package kr.cosine.discordauth.util

import org.bukkit.scheduler.BukkitRunnable

class Timer(
    private val onTimer: () -> Boolean,
    private val onTimeOut: () -> Unit = {},
    private val onCancel: () -> Unit = {}
) : BukkitRunnable() {
    private var time = 30

    override fun run() {
        if (time <= 0) {
            cancel()
            onTimeOut()
            return
        }
        if (onTimer()) {
            cancel()
            return
        }
        time--
    }

    override fun cancel() {
        super.cancel()
        onCancel()
    }
}