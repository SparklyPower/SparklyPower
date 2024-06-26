package net.perfectdreams.pantufa.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class SparklyPowerConfig(
    val server: SparklyPowerServerConfig,
    val guild: SparklyPowerGuildConfig
) {
    @Serializable
    data class SparklyPowerServerConfig(
        val lorittaPort: Int,
        val perfectDreamsLobbyPort: Int,
        val perfectDreamsSurvivalPort: Int,
        val perfectDreamsBungeeIp: String,
        val perfectDreamsBungeePort: Int,
        val sparklyPowerSurvival: ServerRPC
    ) {
        @Serializable
        data class ServerRPC(
            val apiUrl: String
        )
    }

    @Serializable
    data class SparklyPowerGuildConfig(
        val idLong: Long,
        val staffRoleId: Long,
        val whitelistedChannels: List<Long>,
        val whitelistedRoles: List<Long>,
        val staffChannelId: Long,
        val pantufaPrintShopChannelId: Long,
        val sparklySkinsLogChannelId: Long,
        val memberRoleId: Long,
        val ownerRoleId: Long,
        val adminRoleId: Long,
        val coordRoleId: Long,
        val modRoleId: Long,
        val supportRoleId: Long,
        val builderRoleId: Long,
        val devRoleId: Long,
        val vipRoleId: Long,
        val influencerRoleId: Long,
        val starRoleId: Long
    )
}