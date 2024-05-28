package net.perfectdreams.pantufa.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class GrafanaConfig(
    val url: String,
    val token: String
)