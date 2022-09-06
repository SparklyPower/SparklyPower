package net.perfectdreams.dreamchat.utils

import kotlinx.serialization.Serializable

@Serializable
data class MinecraftEmoji(
    val name: String,
    val character: String
)