package kr.hqservice.auth.extension

import org.bukkit.plugin.Plugin

fun Plugin.disablePlugin(message: String) {
    pluginLoader.disablePlugin(this)
    logger.warning(message)
}