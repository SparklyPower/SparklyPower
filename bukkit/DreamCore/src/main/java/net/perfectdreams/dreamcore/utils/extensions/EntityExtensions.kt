package net.perfectdreams.dreamcore.utils.extensions

import kotlinx.coroutines.future.await
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.DreamUtils
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.concurrent.CompletableFuture

fun Entity.teleportToServerSpawn(teleportCause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) = this.teleport(DreamCore.INSTANCE.spawn!!, teleportCause)
fun Entity.teleportToServerSpawnAsync(teleportCause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) = this.teleportAsync(DreamCore.INSTANCE.spawn!!, teleportCause)

suspend fun Entity.teleportAwait(location: Location, teleportCause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) = this.teleportAsync(location, teleportCause).await()

/**
 * Teleports the entity and plays a nice teleportation effect and sound effect at the entity's current location
 */
fun Entity.teleportToServerSpawnWithEffects(teleportCause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) {
    teleportToServerSpawn(teleportCause)
    playTeleportEffects()
}

/**
 * Teleports the entity and plays a nice teleportation effect and sound effect at the entity's current location
 */
suspend fun Entity.teleportToServerSpawnWithEffectsAwait(teleportCause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) {
    val c = teleportToServerSpawnAsync(teleportCause)
    c.await()
    // Only player the effects AFTER the teleport
    playTeleportEffects()
}

/**
 * Plays a nice teleportation effect and sound effect at the entity's current location
 */
fun Entity.playTeleportEffects() {
    if (this is Player) {
        this.playSound(this, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, DreamUtils.random.nextFloat(0.8f, 1.2f))
    }
    world.spawnParticle(Particle.HAPPY_VILLAGER, location.add(0.0, 1.0, 0.0), 25, 0.5, 0.5, 0.5)
    world.spawnParticle(Particle.ENCHANT, location.add(0.0, 1.0, 0.0), 25, 0.5, 0.5, 0.5)
}

/**
 * Teleports the entity and plays a nice teleportation effect and sound effect at the entity's current location
 */
fun Entity.teleportWithEffects(location: Location, teleportCause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) {
    teleport(location, teleportCause)
    playTeleportEffects()
}