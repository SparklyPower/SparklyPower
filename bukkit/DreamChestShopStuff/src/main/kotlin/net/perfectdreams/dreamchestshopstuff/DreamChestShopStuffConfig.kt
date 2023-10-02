package net.perfectdreams.dreamchestshopstuff

import kotlinx.serialization.Serializable

@Serializable
data class DreamChestShopStuffConfig(
    val suspiciousChestShopWebhookUrl: String
)