package net.sparklypower.rpc

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePlayerSkinRequest(
    val requestedById: String,
    val skinId: String,
    val playerTextureValue: String,
    val playerTextureSignature: String
)

@Serializable
sealed class UpdatePlayerSkinResponse {
    @Serializable
    data class Success(val playerIsOnline: Boolean) : UpdatePlayerSkinResponse()
}