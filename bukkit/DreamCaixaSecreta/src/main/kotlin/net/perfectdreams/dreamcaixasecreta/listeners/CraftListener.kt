package net.perfectdreams.dreamcaixasecreta.listeners

import net.perfectdreams.dreamcaixasecreta.DreamCaixaSecreta
import net.perfectdreams.dreamcore.utils.get
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe

class CraftListener(private val m: DreamCaixaSecreta) : Listener {
    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val recipe = event.recipe as? ShapelessRecipe ?: return

        // If it is a repair craft, ignore
        if (event.isRepair)
            return

        // If the recipe key of the event doesn't match our combine boxes recipe key, then bail out!
        if (recipe.key != m.COMBINE_BOXES_KEY)
            return

        val inventoryMatrix = event.inventory.matrix.filterNotNull()

        val firstItemInMatrix = inventoryMatrix.first() // Should not be null because the recipe must have 2 ingredients
        val firstItemInMatrixMetadata = getCaixaSecretaMetadata(firstItemInMatrix)

        val level = firstItemInMatrixMetadata.first
        if (level == null || level == 4) {
            event.inventory.result = null
            return
        }

        val lastItemInMatrix = inventoryMatrix.last() // Should not be null because the recipe must have 2 ingredients
        val lastItemInMatrixMetadata = getCaixaSecretaMetadata(lastItemInMatrix)

        if (firstItemInMatrixMetadata != lastItemInMatrixMetadata) {
            event.inventory.result = null
            return
        }

        event.inventory.result = m.generateCaixaSecreta(level + 1, firstItemInMatrixMetadata.second)
    }

    private fun getCaixaSecretaMetadata(itemStack: ItemStack) = itemStack.itemMeta.persistentDataContainer.get(DreamCaixaSecreta.CAIXA_SECRETA_LEVEL_KEY) to itemStack.itemMeta.persistentDataContainer.get(DreamCaixaSecreta.CAIXA_SECRETA_WORLD_KEY)
}