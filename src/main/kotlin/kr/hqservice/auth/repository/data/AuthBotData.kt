package kr.hqservice.auth.repository.data

import org.bukkit.configuration.file.YamlConfiguration

abstract class AuthBotData(val section: String) : Readable<YamlConfiguration>, Writable<YamlConfiguration> {

    abstract var isChanged: Boolean
}