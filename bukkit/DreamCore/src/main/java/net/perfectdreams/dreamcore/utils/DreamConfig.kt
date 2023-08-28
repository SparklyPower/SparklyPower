package net.perfectdreams.dreamcore.utils

import kotlinx.serialization.Serializable

@Serializable
data class DreamConfig(
    val serverName: String,
    val bungeeName: String,
    val strings: Strings,
    val socket: SocketConfig,
    val networkDatabase: NetworkDatabaseConfig,
    val servers: NetworkServersConfig,
    val blacklistedWorldsTeleport: List<String>,
    val blacklistedRegionsTeleport: List<String>,
    val allowedCommandsDuringEvents: List<String>,
    val discord: DiscordConfig
) {
    @Serializable
    data class Strings(
        val withoutPermission: String,
        val staffPermission: String
    )

    @Serializable
    data class SocketConfig(
        val port: Int
    )

    @Serializable
    data class NetworkDatabaseConfig(
        val user: String,
        val ip: String,
        val port: Int,
        val password: String?,
        val databaseName: String,
        val tablePrefix: String = ""
    )

    @Serializable
    data class NetworkServersConfig(
        val bungeeCord: NetworkServerConfig,
        val lobby: NetworkServerConfig,
        val survival: NetworkServerConfig
    ) {
        @Serializable
        data class NetworkServerConfig(
            val bungeeName: String,
            val rpcAddress: String
        )
    }

    @Serializable
    data class DiscordConfig(
        val eventAnnouncementChannelId: String,
        val webhooks: WebhooksConfig
    ) {
        @Serializable
        data class WebhooksConfig(
            val news: String,
            val warn: String,
            val info: String,
            val error: String
        )
    }
}