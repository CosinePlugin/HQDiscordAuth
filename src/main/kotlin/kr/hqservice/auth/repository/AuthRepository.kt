package kr.hqservice.auth.repository

import kr.hqservice.auth.HQDiscordAuth
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

class AuthRepository(
    plugin: HQDiscordAuth
) : Serializable {

    private companion object {
        const val path = "authorized.yml"
    }

    private val file = File(plugin.dataFolder, path)
    private val config = YamlConfiguration.loadConfiguration(file)

    private val authorizedPlayers = mutableSetOf<UUID>()

    var isChanged = true

    override fun load() {
        if (!config.contains("authorized-players")) return
        authorizedPlayers.addAll(config.getStringList("authorized-players").map(UUID::fromString))
    }

    override fun save() {
        config.set("authorized-players", authorizedPlayers.map(UUID::toString))
        config.save(file)
        isChanged = false
    }

    fun contains(uuid: UUID) = authorizedPlayers.contains(uuid)

    fun add(uuid: UUID) {
        authorizedPlayers.add(uuid)
        isChanged = true
    }

    fun remove(uuid: UUID) {
        authorizedPlayers.remove(uuid)
        isChanged = true
    }
}