package net.sparklypower.sparklyneonvelocity.config

import kotlinx.serialization.Serializable

@Serializable
data class SparklyNeonVelocityConfig(
    val database: DatabaseConfig,
    val listeners: List<VelocityListenerConfig>,
    val discord: DiscordConfig,
    val socketPort: Int?,
    val rpcPort: Int,
    val alwaysForceOfflineMode: Boolean,
    val requireDreamAuthLogin: Boolean
) {
    @Serializable
    data class DatabaseConfig(
        val database: String,
        val address: String,
        val username: String,
        val password: String
    )

    @Serializable
    data class VelocityListenerConfig(
        val name: String,
        val bind: String,
        val proxyProtocol: Boolean
    )

    @Serializable
    data class DiscordConfig(
        val webhooks: DiscordWebhooks
    ) {
        @Serializable
        data class DiscordWebhooks(
            val punishmentWebhook: String,
            val adminChatWebhook: String,
            val discordAccountAssociationsWebhook: String
        )
    }
}