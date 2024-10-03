package kr.cosine.discordauth.config.module

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.cosine.discordauth.config.AuthorizedPlayerConfig
import kr.cosine.discordauth.config.SettingConfig
import kr.hqservice.framework.bukkit.core.HQBukkitPlugin
import kr.hqservice.framework.bukkit.core.component.module.Module
import kr.hqservice.framework.bukkit.core.component.module.Setup
import kr.hqservice.framework.bukkit.core.component.module.Teardown
import kr.hqservice.framework.bukkit.core.coroutine.bukkitDelay
import kr.hqservice.framework.bukkit.core.coroutine.element.TeardownOptionCoroutineContextElement

@Module
class ConfigModule(
    private val plugin: HQBukkitPlugin,
    private val settingConfig: SettingConfig,
    private val authorizedPlayerConfig: AuthorizedPlayerConfig
) {
    @Setup
    fun setup() {
        settingConfig.load()
        authorizedPlayerConfig.load()

        plugin.launch(Dispatchers.IO + TeardownOptionCoroutineContextElement(true)) {
            while (isActive) {
                bukkitDelay(12000) // 10분
                authorizedPlayerConfig.save()
            }
        }
    }

    @Teardown
    fun teardown() {
        authorizedPlayerConfig.save()
    }
}