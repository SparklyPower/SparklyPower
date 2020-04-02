package net.perfectdreams.dreamcore.utils.extensions

import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent
import net.perfectdreams.dreamcore.DreamCore

fun Entity.teleportToServerSpawn(teleportCause: PlayerTeleportEvent.TeleportCause = PlayerTeleportEvent.TeleportCause.PLUGIN) = this.teleport(DreamCore.INSTANCE.spawn!!, teleportCause)