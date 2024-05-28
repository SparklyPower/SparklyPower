package net.sparklypower.rpc

import kotlinx.serialization.Serializable

@Serializable
sealed class SparklySurvivalRPCRequest {
    @Serializable
    class GetSonecasRequest(
        val playerUniqueId: String
    ) : SparklySurvivalRPCRequest()

    @Serializable
    class TransferSonecasRequest(
        val giverId: String,
        val receiverId: String,
        val quantity: Double
    ) : SparklySurvivalRPCRequest()
}