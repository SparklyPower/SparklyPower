package net.perfectdreams.dreamassinaturas.data

import org.bukkit.Bukkit
import org.bukkit.Location
import java.time.Instant
import java.util.*

data class Assinatura(
    val id: Long,
    val signedBy: UUID,
    val signedAt: Instant,
    val location: AssinaturaLocation
) {
    data class AssinaturaLocation(
        val world: String,
        val x: Int,
        val y: Int,
        val z: Int
    ) {
        fun toBukkitLocation() = Location(Bukkit.getWorld(world), x.toDouble(), y.toDouble(), z.toDouble())
    }
}