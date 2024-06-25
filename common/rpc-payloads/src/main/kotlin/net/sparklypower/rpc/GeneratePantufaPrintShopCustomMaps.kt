package net.sparklypower.rpc

import kotlinx.serialization.Serializable

@Serializable
class GeneratePantufaPrintShopCustomMapsRequest(
    val requestId: Long,
    val approvedById: String
)

@Serializable
sealed class GeneratePantufaPrintShopCustomMapsResponse {
    @Serializable
    data class Success(
        val requestedById: String,
        val cost: Long
    ): GeneratePantufaPrintShopCustomMapsResponse()

    @Serializable
    data object NotEnoughPesadelos : GeneratePantufaPrintShopCustomMapsResponse()

    @Serializable
    data object UnknownPlayer : GeneratePantufaPrintShopCustomMapsResponse()

    @Serializable
    data object UnknownMapRequest : GeneratePantufaPrintShopCustomMapsResponse()

    @Serializable
    data object RequestAlreadyApproved : GeneratePantufaPrintShopCustomMapsResponse()

    @Serializable
    data object PluginUnavailable : GeneratePantufaPrintShopCustomMapsResponse()
}