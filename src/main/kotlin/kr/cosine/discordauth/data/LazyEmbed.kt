package kr.cosine.discordauth.data

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

data class LazyEmbed(
    private val color: Color,
    private val title: String,
    private val titleUrl: String,
    private val description: String,
    private val thumbnail: String,
    private val image: String,
    private val authorName: String,
    private val authorUrl: String,
    private val authorIconUrl: String,
    private val fields: List<Triple<String, String, Boolean>>,
    private val footer: String
) {
    fun toEmbed(replace: (String) -> String = { it }): MessageEmbed {
        return EmbedBuilder().apply {
            setColor(color)

            if (title.isNotEmpty() || titleUrl.isNotEmpty()) {
                setTitle(replace(title), replace(titleUrl))
            }

            if (description.isNotEmpty()) {
                setDescription(replace(description))
            }

            if (thumbnail.isNotEmpty()) {
                setThumbnail(replace(thumbnail))
            }

            if (image.isNotEmpty()) {
                setImage(replace(image))
            }

            if (authorName.isNotEmpty() || authorUrl.isNotEmpty() || authorIconUrl.isNotEmpty()) {
                setAuthor(replace(authorName), replace(authorUrl), replace(authorIconUrl))
            }

            this@LazyEmbed.fields.forEach { (name, value, inline) ->
                addField(replace(name), replace(value), inline)
            }

            if (footer.isNotEmpty()) {
                setFooter(replace(footer))
            }
        }.build()
    }
}