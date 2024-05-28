package net.perfectdreams.pantufa.rpc

import kotlinx.serialization.Serializable

@Serializable
sealed interface PantufaRPCResponse

@Serializable
sealed interface GetDiscordUserResponse : PantufaRPCResponse {
    @Serializable
    class Success(
        val id: Long,
        val name: String,
        val discriminator: String,
        val avatarId: String?,
        val bot: Boolean,
        val system: Boolean,
        val flags: Int
    ) : GetDiscordUserResponse

    object NotFound : GetDiscordUserResponse
}

@Serializable
sealed interface BanSparklyPowerPlayerLorittaBannedResponse : PantufaRPCResponse {
    @Serializable
    class Success(
        val uniqueId: String,
        val userName: String
    ) : BanSparklyPowerPlayerLorittaBannedResponse

    @Serializable
    object NotFound : BanSparklyPowerPlayerLorittaBannedResponse
}