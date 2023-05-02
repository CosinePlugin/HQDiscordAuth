package kr.hqservice.auth.repository

import kr.hqservice.auth.HQDiscordAuth
import kr.hqservice.auth.extension.filterKClass
import kr.hqservice.auth.repository.data.AuthBotData
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.IllegalArgumentException
import kotlin.reflect.KClass

class AuthConfigRepository(
    private val plugin: HQDiscordAuth
) : Serializable, Registrable<AuthBotData> {

    private companion object {
        const val path = "config.yml"
    }

    private lateinit var file: File
    private lateinit var config: YamlConfiguration

    private val dataSet = mutableSetOf<AuthBotData>()

    override fun load() {
        if (plugin.getResource(path) != null) {
            plugin.saveResource(path, false)
        }
        file = File(plugin.dataFolder, path)
        config = YamlConfiguration.loadConfiguration(file)
    }

    override fun save() {
        dataSet.filter { it.isChanged }.forEach { it.write(config) }
        config.save(file)
    }

    override fun register(vararg kClasses: KClass<out AuthBotData>) {
        kClasses.forEach { clazz ->
            if (dataSet.firstOrNull { clazz.isInstance(it) } == null) {
                val instance = clazz.constructors.first().call()
                instance.read(config)
                dataSet.add(instance)
            }
        }
    }

    override fun unregister(vararg kClasses: KClass<out AuthBotData>) {
        dataSet.removeIf { setting ->
            kClasses.any { it.isInstance(setting) }
        }
    }

    override fun isRegistered(vararg kClasses: KClass<out AuthBotData>): Boolean {
        return kClasses.all { dataSet.filterKClass(it).isNotEmpty() }
    }

    override fun <K : AuthBotData> get(clazz: KClass<out K>): K {
        val filter = dataSet.filterKClass(clazz)
        if (filter.isEmpty()) {
            throw IllegalArgumentException("데이터가 등록되어 있지 않습니다.")
        } else {
            return filter.first()
        }
    }
}