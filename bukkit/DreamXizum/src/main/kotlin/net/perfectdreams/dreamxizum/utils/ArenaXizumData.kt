package net.perfectdreams.dreamxizum.utils

import kotlinx.serialization.Serializable

@Serializable
data class ArenaXizumData(
    val name: String,
    val isReady: Boolean,
    val location1: Location,
    val location2: Location
) {
    @Serializable
    data class Location(
        val world: String,
        val x: Double,
        val y: Double,
        val z: Double,
        val yaw: Float,
        val pitch: Float
    )
}