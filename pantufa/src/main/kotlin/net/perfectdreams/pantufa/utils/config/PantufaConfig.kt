package net.perfectdreams.pantufa.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class PantufaConfig(
    val token: String,
    val rpcPort: Int,
    val discordInteractions: DiscordInteractionsConfig,
    val postgreSqlSparklyPower: PostgreSqlConfig,
    val postgreSqlLuckPerms: PostgreSqlConfig,
    val postgreSqlLoritta: PostgreSqlConfig,
    val mariaDbCraftConomy: PostgreSqlConfig,
    val grafana: GrafanaConfig
)