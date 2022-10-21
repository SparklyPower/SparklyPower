package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.*
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import kotlin.math.ceil
import kotlin.math.min

class BlockCraftListener(val m: DreamCustomItems) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: CraftItemEvent) {
        val recipe = e.recipe
        if (recipe is Keyed) {
            // Are any of the recipes requiring a legit ruby?
			val recipeKey = m.customRecipes.asSequence().filterIsInstance<CustomCraftingRecipe>().any { (it.recipe as? Keyed)?.key?.key == recipe.key.key }

            // If yes, we will cancel the event if it doesn't match
            if (recipeKey && e.inventory.any { it.type == Material.PRISMARINE_SHARD && (!it.itemMeta.hasCustomModelData() || it.itemMeta.customModelData != 1) })
                e.isCancelled = true
        }
    }
}