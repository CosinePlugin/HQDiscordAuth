package kr.hqservice.auth.builder

import kr.hqservice.auth.repository.data.impl.AuthBotMessage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

class AuthEmbedBuilder(private val authBotMessage: AuthBotMessage) {

    private var embed = EmbedBuilder()

    fun setDeafult(): AuthEmbedBuilder {
        embed.apply {
            setColor(Color.GREEN)
            setTitle(authBotMessage.title)
            setDescription(authBotMessage.description)
            authBotMessage.tumbnail?.let { setThumbnail(it) }
            authBotMessage.authorName?.let { name ->
                authBotMessage.authorUrl?.let { url ->
                    setAuthor(name, url)
                }
            }
            authBotMessage.image?.let { setImage(it) }
            authBotMessage.footer?.let { setFooter(it) }
            authBotMessage.field.forEach {
                addField(it.fieldName, it.fieldValue, it.inline)
            }
        }
        return this
    }

    fun build(): MessageEmbed {
        return embed.build()
    }
}