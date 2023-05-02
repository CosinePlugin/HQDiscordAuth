package kr.hqservice.auth.repository.data.impl

import kr.hqservice.auth.repository.data.AuthBotData
import net.dv8tion.jda.api.OnlineStatus
import org.bukkit.configuration.file.YamlConfiguration

class AuthBotSetting : AuthBotData("bot-setting") {

    data class DiscordId(var guildId: Long, var authId: Long, var logId: Long? = null)

    data class BotInfo(var status: OnlineStatus, var statusMessage: String)

    data class ChangedRole(var enable: Boolean, var roles: MutableList<Long>)

    data class ChangedName(var enable: Boolean, var name: String)

    override var isChanged: Boolean = false

    lateinit var token: String
        private set

    var logEnable: Boolean = true
        private set

    lateinit var discordId: DiscordId
        private set

    lateinit var botInfo: BotInfo
        private set

    lateinit var changedRole: ChangedRole
        private set

    lateinit var changedName: ChangedName
        private set

    override fun read(data: YamlConfiguration) {
        data.getConfigurationSection(section)?.apply {
            token = getString("token")!!

            logEnable = getBoolean("log")

            discordId = DiscordId(
                getLong("id.guild"),
                getLong("id.auth"),
                if (getBoolean("log")) getLong("id.log") else null
            )

            botInfo = BotInfo(
                OnlineStatus.values().find { it.name == getString("info.status") } ?: OnlineStatus.ONLINE,
                getString("info.status-message")
            )

            getConfigurationSection("success-auth")?.apply {
                changedRole = ChangedRole(
                    getBoolean("changed-role.enable"),
                    getLongList("changed-role.roles")
                )
                changedName = ChangedName(
                    getBoolean("changed-name.enable"),
                    getString("changed-name.name")
                )
            }
        }
    }

    override fun write(data: YamlConfiguration) {}
}