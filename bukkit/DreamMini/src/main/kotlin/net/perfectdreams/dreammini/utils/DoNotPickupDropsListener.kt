package net.perfectdreams.dreammini.utils

import net.perfectdreams.dreammini.DreamMini
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

class DoNotPickupDropsListener(val m: DreamMini) : Listener {
    @EventHandler
    fun onPickup(e: EntityPickupItemEvent) {
        val entity = e.entity as? Player // Apenas players
                ?: return

        val filteredDrops = m.dropsBlacklist[entity]?.contents

        if (e.item.itemStack != null && filteredDrops?.any { it?.type == e.item.itemStack?.type } == true) {
            e.isCancelled = true
        }
    }
}