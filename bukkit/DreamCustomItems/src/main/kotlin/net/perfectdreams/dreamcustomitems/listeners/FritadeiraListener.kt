package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class FritadeiraListener(val m: DreamCustomItems) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        val clickedBlock = e.clickedBlock ?: return
        val heldItem = e.item ?: return

        // Lava Cauldron do not have state!
        if (clickedBlock.type == Material.LAVA_CAULDRON && heldItem.type == Material.POTATO) {
            e.isCancelled = true

            val potatoAmount = heldItem.amount

            // The player can fry 64 potatos at the same time
            heldItem.amount = 0
            e.player.inventory.addItem(
                CustomItems.FRENCH_FRIES.clone()
                    .apply {
                        this.amount = potatoAmount
                    }
            )

            clickedBlock.type = Material.CAULDRON
        }
    }
}