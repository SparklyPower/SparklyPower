package net.perfectdreams.dreamcore.utils.extensions

import kotlinx.coroutines.future.await
import net.perfectdreams.dreamcore.DreamCore
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent

fun Entity.teleportToServerSpawn(teleportCause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) = this.teleport(DreamCore.INSTANCE.spawn!!, teleportCause)
suspend fun Entity.teleportAwait(location: Location, teleportCause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) = this.teleportAsync(location, teleportCause).await()