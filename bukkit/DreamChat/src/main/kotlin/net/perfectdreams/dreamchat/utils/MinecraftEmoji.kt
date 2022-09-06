package net.perfectdreams.dreamchat.utils

import kotlinx.serialization.Serializable

data class MinecraftEmoji(
    val name: String,
    val character: String
) {
    val chatFormat = ":$name:"
}