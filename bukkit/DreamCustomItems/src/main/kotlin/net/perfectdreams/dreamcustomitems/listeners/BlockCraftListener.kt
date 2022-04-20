package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreamcustomitems.utils.isMagnet
import net.perfectdreams.dreamcustomitems.utils.repairMagnetKey
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ShapelessRecipe

class BlockCraftListener(val m: DreamCustomItems) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: CraftItemEvent) {
        val recipe = e.recipe
        if (recipe is Keyed) {
			val recipeKey = when(recipe.key.key) {
				"microwave" -> true
				"superfurnace" -> true
				else -> false				
			}

            if (recipeKey && e.inventory.any { it.type == Material.PRISMARINE_SHARD && (!it.itemMeta.hasCustomModelData() || it.itemMeta.customModelData != 1) })
                e.isCancelled = true
        }
    }

    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) =
        (event.recipe as? ShapelessRecipe)?.let {
            if (it.key != repairMagnetKey) return@let

            with (event.inventory.matrix!!.filterNotNull()) {
                val magnet = firstOrNull(isMagnet) ?: return event.inventory.setResult(null)
                val isNormalMagnet = magnet.itemMeta.customModelData == 1

                event.inventory.result = if (isNormalMagnet) CustomItems.MAGNET.clone() else CustomItems.MAGNET_2.clone()
            }
        } ?: Unit
}
