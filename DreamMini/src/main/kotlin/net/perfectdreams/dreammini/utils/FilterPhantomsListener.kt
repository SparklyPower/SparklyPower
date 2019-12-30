package net.perfectdreams.dreammini.utils

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class FilterPhantomsListener(val m: DreamMini) : Listener {
    @EventHandler
    fun onPhantomSpawn(e: PhantomPreSpawnEvent) {
        val spawningEntity = e.spawningEntity as? Player ?: return

        if (!m.phantomWhitelist.contains(spawningEntity.uniqueId)) {
            e.isCancelled = true
        }
    }
}