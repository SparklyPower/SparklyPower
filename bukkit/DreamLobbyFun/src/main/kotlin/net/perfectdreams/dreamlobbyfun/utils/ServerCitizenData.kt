package net.perfectdreams.dreamlobbyfun.utils

import kotlinx.serialization.Serializable

@Serializable
data class ServerCitizenData(
    val citizenId: Int,
    val serverName: String,
    val fancyServerName: String
)