package net.perfectdreams.dreamcaixasecreta.listeners

import net.perfectdreams.dreamcaixasecreta.DreamCaixaSecreta
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe

class CraftListener(private val m: DreamCaixaSecreta) : Listener {
    private val world = "caixaSecretaWorld"
    private val level = "caixaSecretaLevel"
    private val key = m.COMBINE_BOXES_KEY

    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) =
        (event.recipe as? ShapelessRecipe)?.let {
            with (event) {
                if (isRepair) return@let
                if (it.key != key) return@let

                with (inventory.matrix!!.filterNotNull()) {
                    val meta = first().metadata

                    var level = meta.first ?: return inventory.cancel()
                    if (level == 4) return inventory.cancel()
                    if (meta != last().metadata) return inventory.cancel()

                    inventory.result = m.generateCaixaSecreta(++level, meta.second)
                }
            }
        } ?: Unit

    private val ItemStack.metadata get() = getStoredMetadata(level)?.toIntOrNull() to getStoredMetadata(world)
    private fun CraftingInventory.cancel() { result = null }
}