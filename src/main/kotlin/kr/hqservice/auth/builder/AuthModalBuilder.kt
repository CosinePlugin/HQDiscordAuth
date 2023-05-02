package kr.hqservice.auth.builder

import kr.hqservice.auth.listener.DiscordListener
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

class AuthModalBuilder {

    private companion object {
        const val title = "인증"
        const val label = "입력"
        const val placeHolder = "발급받은 코드를 입력해주세요."
        const val length = 6
    }

    fun build(): Modal {
        val input = TextInput.create(DiscordListener.codeTypingId, label, TextInputStyle.SHORT)
            .setPlaceholder(placeHolder)
            .setMinLength(length)
            .setMaxLength(length)
            .setRequired(true)
            .build()

        return Modal.create(DiscordListener.modalId, title)
            .addActionRow(ActionRow.of(input).components)
            .build()
    }
}