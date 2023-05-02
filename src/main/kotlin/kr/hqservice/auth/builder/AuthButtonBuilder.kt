package kr.hqservice.auth.builder

import kr.hqservice.auth.controller.AuthBotController
import kr.hqservice.auth.repository.data.impl.AuthBotMessage
import net.dv8tion.jda.api.interactions.components.buttons.Button

class AuthButtonBuilder(private val authBotMessage: AuthBotMessage) {

    fun build(): Button {
        return Button.of(authBotMessage.buttonStyle, AuthBotController.buttonId, authBotMessage.buttonInfo)
    }
}