package net.perfectdreams.dreammapwatermarker

import kotlinx.serialization.Serializable

@Serializable
data class DreamMapWatermarkerConfig(
    val generateLorittaFigurittas: Boolean,
    val lorittaInternalApiUrl: String
)