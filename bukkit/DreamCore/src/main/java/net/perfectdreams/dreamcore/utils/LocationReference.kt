package net.perfectdreams.dreamcore.utils

import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.Location

@Serializable
data class LocationReference(
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float
) {
    companion object {
        fun fromBukkit(location: Location) = LocationReference(location.world.name, location.x, location.y, location.z, location.yaw, location.pitch)
    }

    fun toBukkit() = Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch)
}