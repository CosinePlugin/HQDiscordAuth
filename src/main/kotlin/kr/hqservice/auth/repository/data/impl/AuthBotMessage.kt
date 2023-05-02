package kr.hqservice.auth.repository.data.impl

import kr.hqservice.auth.repository.data.AuthBotData
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.bukkit.configuration.file.YamlConfiguration

class AuthBotMessage : AuthBotData("message-setting") {

    override var isChanged: Boolean = false

    data class EmbedField(var fieldName: String, var fieldValue: String, var inline: Boolean)

    var title = "인증"
        private set

    var description = "아래 버튼 클릭 시 인증을 진행합니다."
        private set

    var tumbnail: String? = null
        private set

    var image: String? = null
        private set

    var authorName: String? = null
        private set

    var authorUrl: String? = null
        private set

    var field = mutableListOf<EmbedField>()
        private set

    var footer: String? = null
        private set

    var buttonInfo = "인증"
        private set

    var buttonStyle = ButtonStyle.SUCCESS
        private set

    override fun read(data: YamlConfiguration) {
        data.getConfigurationSection(section)?.apply {
            getConfigurationSection("embed")?.apply {
                title = getString("title")

                description = getString("description")

                tumbnail = getString("tumbnail")

                image = getString("image")

                authorName = getString("author.name")
                authorUrl = getString("author.url")

                field = getStringList("field").map {
                    val split = it.split(", ")
                    EmbedField(split[0], split[1], split[2].toBoolean())
                }.toMutableList()

                footer = getString("footer")
            }
            getConfigurationSection("button")?.apply {
                buttonInfo = getString("info")

                buttonStyle = ButtonStyle.values().find { it.name == getString("style") } ?: ButtonStyle.SUCCESS
            }
        }
    }

    override fun write(data: YamlConfiguration) {}
}