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
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import kotlin.math.ceil
import kotlin.math.min

class BlockCraftListener(val m: DreamCustomItems) : Listener {
    private val AMETHYST = Material.AMETHYST_SHARD
    private val COPPER = Material.COPPER_INGOT

    private val toTakeMap = mutablePlayerMapOf<Int>()
    private val recipeDurability = (weirdMagnetDurability / 32)

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

                val maxDurability = if (isNormalMagnet) magnetDurability else weirdMagnetDurability
                val currentDurability = magnet.itemMeta.persistentDataContainer.get(magnetKey, PersistentDataType.INTEGER) ?: maxDurability

                if (currentDurability == maxDurability) return event.inventory.setResult(null)

                val maxItCanUse = ceil((maxDurability - currentDurability).toFloat() / recipeDurability).toInt()
                val howManyToUse = min(first { it.type == COPPER }.amount, first { it.type == AMETHYST }.amount).let {
                    if (it > maxItCanUse) maxItCanUse else it
                }
                val newDurability = (currentDurability + howManyToUse * recipeDurability).let { if (it > maxDurability) maxDurability else it }

                toTakeMap[event.inventory.viewers.first() as Player] = howManyToUse

                val repairedMagnet = if (isNormalMagnet) CustomItems.MAGNET.clone() else CustomItems.MAGNET_2.clone()
                event.inventory.result = repairedMagnet.updateMagnetLore(newDurability, maxDurability)
            }
        } ?: Unit

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) =
        (event.clickedInventory as? CraftingInventory)?.let { inventory ->
            if (event.slotType != InventoryType.SlotType.RESULT) return@let
            val recipe = inventory.recipe as? ShapelessRecipe ?: return@let
            if (event.click == ClickType.MIDDLE) return@let
            if (recipe.key != repairMagnetKey) return@let

            with (inventory.matrix!!) {
                val howManyToTake = toTakeMap.remove(inventory.viewers.first() as Player)!!

                first { it?.type == AMETHYST }!!.take(howManyToTake)
                first { it?.type == COPPER }!!.take(howManyToTake)
            }
        } ?: Unit

    private fun ItemStack.take(target: Int) { amount = if (amount > target) amount - target + 1 else 1 }
}