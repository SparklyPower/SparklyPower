package net.perfectdreams.dreamdropparty.utils

import org.bukkit.Bukkit
import org.bukkit.Location

class LocationWrapper(val world: String, val x: Double, val y: Double, val z: Double, val yaw: Float, val pitch: Float) {

    fun toLocation() = Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)
}

fun Location.toWrapper() = LocationWrapper(this.world.name, this.x, this.y, this.z, this.yaw, this.pitch)