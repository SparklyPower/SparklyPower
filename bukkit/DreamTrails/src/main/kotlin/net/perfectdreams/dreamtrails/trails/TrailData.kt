package net.perfectdreams.dreamtrails.trails

import org.bukkit.Material
import org.bukkit.Particle

open class TrailData(
    val name: String,
    val material: Material,
    val particle: Particle,
    val count: Int = 1,
    val offsetX: Double = 0.5,
    val offsetY: Double = 0.3,
    val offsetZ: Double = 0.5,
    val locationOffsetX: Double = 0.0,
    val locationOffsetY: Double = 0.0,
    val locationOffsetZ: Double = 0.0,
    val locationDirectionOffset: Double = -1.0,
    val cooldown: Long = 0,
    val additionalData: (() -> (Any))? = null
)