package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.*
import net.sparklypower.sparklypaper.event.inventory.CraftItemRecipeEvent
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class BlockCraftListener(val m: DreamCustomItems) : Listener {
    // TODO: Is the PrepareItemCraftEvent needed? I don't get it why it is here
    // It is probably to show off the "how the item will look like" on the crafting table, right?
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPrepareCraft(e: PrepareItemCraftEvent) {
        val recipe = e.recipe
        if (recipe is Keyed) {
            if (recipe.key.key == "magnet_repair") {
                when (val result = validateMagnetCraft(e.inventory.matrix)) {
                    is MagnetCraftValidationResult.Success -> {
                        e.inventory.result = result.magnet.clone().meta<Damageable> {
                            damage = 0
                            persistentDataContainer.set(MagnetUtils.MAGNET_DURABILITY, 0)
                        }
                    }

                    else -> {
                        e.inventory.result = null
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    fun onBreak(e: CraftItemRecipeEvent) {
        val recipe = e.recipe

        if (recipe is Keyed) {
            if (recipe.key.key == "magnet_repair") {
                when (val result = validateMagnetCraft(e.craftingMatrix)) {
                    is MagnetCraftValidationResult.Success -> {
                        // Needs to be -1 since the recipe by itself already removes 1 when crafting the item
                        result.amethyst.amount -= (result.type.requiredAmethystToRepair - 1)
                        result.copper.amount -= (result.type.requiredCopperToRepair - 1)

                        e.result = result.magnet.meta<Damageable> {
                            damage = 0
                            persistentDataContainer.set(MagnetUtils.MAGNET_DURABILITY, 0)
                        }
                    }

                    else -> {
                        e.isCancelled = true
                        // TODO: I think we don't need this?
                        //  we technically don't need this because if the event is cancelled the player can't drag the item already
                        // e.result = null
                    }
                }
                return
            }

            // Get if this recipe is a custom recipe
			val customRecipe = m.customRecipes.asSequence().filterIsInstance<CustomCraftingRecipe>()
                .firstOrNull { (it.recipe as? Keyed)?.key?.key == recipe.key.key }

            // It is a custom recipe, so now we need if our recipe matches
            if (customRecipe != null && customRecipe.checkRemappedItems) {
                val valid = e.craftingMatrix
                    .filterNotNull()
                    .all {
                        val remappedItem = customRecipe.itemRemapper.invoke(it)

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

    private fun validateMagnetCraft(matrix: Array<ItemStack?>): MagnetCraftValidationResult {
        val amethyst = matrix[3]
        val magnet = matrix[4]
        val copper = matrix[5]

        if (amethyst == null || magnet == null || copper == null) {
            return MagnetCraftValidationResult.MissingItems
        }

        val magnetType = MagnetUtils.getMagnetType(magnet) ?: return MagnetCraftValidationResult.NotAMagent

        val hasRequiredAmethystCount = amethyst.amount >= magnetType.requiredAmethystToRepair
        val hasRequiredCopperCount = copper.amount >= magnetType.requiredCopperToRepair

        if (!hasRequiredAmethystCount || !hasRequiredCopperCount)
            return MagnetCraftValidationResult.InsufficientResources

        return MagnetCraftValidationResult.Success(magnet, magnetType, amethyst, copper)
    }

    sealed class MagnetCraftValidationResult {
        class Success(val magnet: ItemStack, val type: MagnetUtils.MagnetType, val amethyst: ItemStack, val copper: ItemStack) : MagnetCraftValidationResult()
        object MissingItems : MagnetCraftValidationResult()
        object NotAMagent : MagnetCraftValidationResult()
        object InsufficientResources : MagnetCraftValidationResult()
    }
}