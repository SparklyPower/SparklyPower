package net.perfectdreams.minecraftmojangapi.data

import kotlinx.serialization.Serializable

@Serializable
class MCTextures(
    val timestamp: Long,
    val profileId: String,
    val profileName: String,
    val signatureRequired: Boolean? = null,
    val textures: Map<String, TextureValue>
)