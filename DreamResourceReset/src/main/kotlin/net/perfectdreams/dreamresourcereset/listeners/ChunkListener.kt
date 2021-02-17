package net.perfectdreams.dreamresourcereset.listeners

import net.perfectdreams.dreamresourcereset.DreamResourceReset
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

class ChunkListener(val m: DreamResourceReset) : Listener {
    @EventHandler
    fun onChunkLoad(e: ChunkLoadEvent) {
        if (e.world.name != "Resources")
            return

        // Save inhabited time on chunk load
        m.cachedInhabitedChunkTimers[e.chunk.chunkKey] = e.chunk.inhabitedTime
    }

    @EventHandler
    fun onChunkUnload(e: ChunkUnloadEvent) {
        if (e.world.name != "Resources")
            return

        // Save inhabited time on chunk unload
        m.cachedInhabitedChunkTimers[e.chunk.chunkKey] = e.chunk.inhabitedTime
    }
}