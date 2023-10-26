package net.perfectdreams.dreamlobbyfun.utils

import kotlinx.serialization.Serializable

@Serializable
data class ServerCitizenData(
    val locationX: Double,
    val locationY: Double,
    val locationZ: Double,
    val locationYaw: Float,
    val locationPitch: Float,
    val serverName: String,
    val fancyServerName: String
)