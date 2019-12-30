package net.perfectdreams.dreamquiz.utils

import org.bukkit.Location

fun Boolean.prettyBoolean(): String {
    return if (this) "§2§lVerdadeiro" else "§4§lFalso"
}

fun Location.toLocationWrapper(): LocationWrapper {
    return LocationWrapper(this.world.name, this.x, this.y, this.z, this.yaw, this.pitch)
}