package kr.hqservice.auth

import kr.hqservice.auth.cache.AuthCache
import kr.hqservice.auth.command.AuthCommand
import kr.hqservice.auth.controller.AuthBotController
import kr.hqservice.auth.repository.AuthConfigRepository
import kr.hqservice.auth.repository.AuthRepository
import kr.hqservice.auth.repository.data.impl.AuthBotMessage
import kr.hqservice.auth.repository.data.impl.AuthBotSetting
import kr.hqservice.auth.repository.data.impl.AuthMessage
import kr.hqservice.auth.runnable.AuthSaveRepository
import kr.ms.core.bstats.Metrics
import org.bukkit.plugin.java.JavaPlugin

class HQDiscordAuth : JavaPlugin() {

    companion object {
        internal lateinit var plugin: HQDiscordAuth
            private set
    }

    lateinit var authCache: AuthCache
        private set

    lateinit var authConfigRepository: AuthConfigRepository
        private set

    lateinit var authBotController: AuthBotController
        private set

    lateinit var authRepository: AuthRepository
        private set

    override fun onLoad() {
        plugin = this
    }

    override fun onEnable() {
        if (server.pluginManager.getPlugin("MS-Core") == null) {
            logger.warning("MS-Core 플러그인을 찾을 수 없어, 플러그인이 비활성화됩니다.")
            server.pluginManager.disablePlugin(this)
            return
        }
        Metrics(this, 18264)

        authCache = AuthCache()

        authConfigRepository = AuthConfigRepository(this)
        authConfigRepository.load()
        authConfigRepository.register(AuthMessage::class, AuthBotMessage::class, AuthBotSetting::class)

        authRepository = AuthRepository(this)
        authRepository.load()

        authBotController = AuthBotController(this)
        authBotController.load()

        server.scheduler.runTaskTimerAsynchronously(this, AuthSaveRepository(authRepository), 2400, 2400)

        getCommand("인증")?.executor = AuthCommand(this)
    }

    override fun onDisable() {
        authBotController.unload()
        authRepository.save()
    }
}