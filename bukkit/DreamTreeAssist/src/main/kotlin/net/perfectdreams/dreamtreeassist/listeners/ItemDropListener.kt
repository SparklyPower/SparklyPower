package net.perfectdreams.dreamtreeassist.listeners

import net.perfectdreams.dreamtreeassist.DreamTreeAssist
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemSpawnEvent

class ItemDropListener(val m: DreamTreeAssist) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onDrop(e: ItemSpawnEvent) {
        // Not a sapling
        if (e.entity.itemStack.type !in DreamTreeAssist.SAPLINGS)
            return

        // Not in the correct world
        if (e.entity.world.name !in DreamTreeAssist.WORLDS)
            return

        m.trackedSaplings.add(e.entity.uniqueId)
    }
}