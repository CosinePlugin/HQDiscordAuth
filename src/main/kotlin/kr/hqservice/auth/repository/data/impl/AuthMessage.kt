package kr.hqservice.auth.repository.data.impl

import kr.hqservice.auth.extension.applyColor
import kr.hqservice.auth.extension.applyText
import kr.hqservice.auth.repository.data.AuthBotData
import org.bukkit.configuration.file.YamlConfiguration

class AuthMessage : AuthBotData("message") {

    companion object {
        var prefix = "§b[ 인증 ]§f"
            private set
    }

    override var isChanged = false

    lateinit var start: MutableList<String>
        private set

    lateinit var success: MutableList<String>
        private set

    lateinit var fail: MutableList<String>
        private set

    lateinit var processAuthorizing: MutableList<String>
        private set

    lateinit var authorized: MutableList<String>
        private set

    lateinit var expiration: MutableList<String>
        private set

    override fun read(data: YamlConfiguration) {
        data.getConfigurationSection(section)?.apply {
            prefix = (getString("prefix") ?: "§b[ 인증 ]§f").applyColor()
            start = getStringList("start").applyText("%prefix%", prefix).applyColor()
            success = getStringList("success").applyText("%prefix%", prefix).applyColor()
            fail = getStringList("fail").applyText("%prefix%", prefix).applyColor()
            processAuthorizing = getStringList("process-authorizing").applyText("%prefix%", prefix).applyColor()
            authorized = getStringList("authorized").applyText("%prefix%", prefix).applyColor()
            expiration = getStringList("expiration").applyText("%prefix%", prefix).applyColor()
        }
    }

    override fun write(data: YamlConfiguration) {}
}