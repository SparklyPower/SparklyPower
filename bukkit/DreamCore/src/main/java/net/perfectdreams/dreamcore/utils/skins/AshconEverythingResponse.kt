package net.perfectdreams.dreamcore.utils.skins

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AshconEverythingResponse(
    val uuid: String,
    val username: String,
    @SerialName("username_history")
    val usernameHistory: List<UsernameHistory>,
    val textures: PlayerTextures
) {
    @Serializable
    data class UsernameHistory(val username: String)

    @Serializable
    data class PlayerTextures(val raw: Property)

    @Serializable
    data class Property(
        val value: String,
        val signature: String
    )
}