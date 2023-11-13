package net.perfectdreams.dreamcore.utils.npc

import kotlinx.serialization.Serializable

@Serializable
data class SkinTexture(
    val value: String,
    val signature: String
)