package kr.cosine.discordauth.config

import com.google.gson.Gson
import kr.cosine.discordauth.registry.AuthorizedPlayerRegistry
import kr.hqservice.framework.global.core.component.Bean
import org.bukkit.plugin.Plugin
import java.io.File

@Bean
class AuthorizedPlayerConfig(
    plugin: Plugin,
    private val authorizedPlayerRegistry: AuthorizedPlayerRegistry
) {
    private val gson = Gson()

    private val file = File(plugin.dataFolder, "authorized-player.json")

    fun load() {
        if (!file.exists()) return
        val authorizedPlayerRegistry = gson.fromJson(
            file.bufferedReader().use { it.readLines().joinToString() },
            AuthorizedPlayerRegistry::class.java
        )
        this.authorizedPlayerRegistry.restore(authorizedPlayerRegistry)
    }

    fun save() {
        if (authorizedPlayerRegistry.isChanged) {
            authorizedPlayerRegistry.isChanged = false
            val json = gson.toJson(authorizedPlayerRegistry)
            file.bufferedWriter().use {
                it.appendLine(json)
                it.flush()
            }
        }
    }
}