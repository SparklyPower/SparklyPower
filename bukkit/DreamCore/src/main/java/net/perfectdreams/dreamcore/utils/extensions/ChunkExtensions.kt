package net.perfectdreams.dreamcore.utils.extensions

import kotlinx.coroutines.future.await
import net.perfectdreams.dreamcore.DreamCore
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerTeleportEvent

suspend fun Chunk.loadAsync(gen: Boolean = true) = this.world.getChunkAtAsync(this.x, this.z, gen).await()