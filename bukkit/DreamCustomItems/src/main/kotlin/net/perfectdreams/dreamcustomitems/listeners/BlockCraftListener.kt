package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.*
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent

class BlockCraftListener(val m: DreamCustomItems) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: CraftItemEvent) {
        val recipe = e.recipe
        if (recipe is Keyed) {
            // Get if this recipe is a custom recipe
			val customRecipe = m.customRecipes.asSequence().filterIsInstance<CustomCraftingRecipe>()
                .firstOrNull { (it.recipe as? Keyed)?.key?.key == recipe.key.key }

            // It is a customr recipe, so now we need if our recipe matches
            if (customRecipe != null) {
                val valid = e.inventory.matrix
                    .filterNotNull()
                    .all {
                        val remappedItem = customRecipe.itemRemapper.invoke(it.type)

                        var itemMetaCheck = remappedItem.hasItemMeta() == it.hasItemMeta()
                        if (remappedItem.hasItemMeta() && it.hasItemMeta()) {
                            itemMetaCheck = remappedItem.itemMeta.hasCustomModelData() == it.itemMeta.hasCustomModelData()
                            if (remappedItem.itemMeta.hasCustomModelData() && it.itemMeta.hasCustomModelData()) {
                                itemMetaCheck = remappedItem.itemMeta.customModelData == it.itemMeta.customModelData
                            }
                        }
                        remappedItem.type == it.type && itemMetaCheck
                    }

                // Any of our remapped items are not valid, so we will cancel it
                if (!valid)
                    e.isCancelled = true
            }
        }
    }
}