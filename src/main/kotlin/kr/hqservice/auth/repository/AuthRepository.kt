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

    private val authorizedPlayers = mutableMapOf<UUID, Long>()

    var isChanged = true

    override fun load() {
        if (!config.contains("authorized-players")) return
        config.getConfigurationSection("authorized-players").let { section ->
            section.getKeys(false).forEach { uuidText ->
                val uuid = UUID.fromString(uuidText)
                val discordId = section.getLong(uuidText)
                authorizedPlayers[uuid] = discordId
            }
        }
    }

    override fun save() {
        config.set("authorized-players", null)
        authorizedPlayers.forEach { (uuid, discordId) ->
            config.set("authorized-players.$uuid", discordId)
        }
        config.save(file)
        isChanged = false
    }

    fun containsKey(uuid: UUID) = authorizedPlayers.containsKey(uuid)

    fun containsValue(discordId: Long) = authorizedPlayers.containsValue(discordId)

    fun get(uuid: UUID): Long? {
        return authorizedPlayers[uuid]
    }

    fun set(uuid: UUID, discordId: Long) {
        authorizedPlayers[uuid] = discordId
        isChanged = true
    }

    fun remove(uuid: UUID) {
        authorizedPlayers.remove(uuid)
        isChanged = true
    }
}