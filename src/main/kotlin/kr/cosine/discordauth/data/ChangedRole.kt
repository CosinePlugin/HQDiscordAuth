package kr.cosine.discordauth.data

import net.dv8tion.jda.api.entities.Role

data class ChangedRole(
    val isEnabled: Boolean,
    val role: Role
)