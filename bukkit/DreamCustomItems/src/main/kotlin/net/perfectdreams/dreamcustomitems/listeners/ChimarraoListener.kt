package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ChimarraoListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onConsume(e: PlayerItemConsumeEvent) {
        if (e.item.type == Material.MILK_BUCKET && e.item.hasItemMeta()) {
            val itemMeta = e.item.itemMeta
            if (!itemMeta.hasCustomModelData())
                return

            val isChimarrao = itemMeta.customModelData == 1 || itemMeta.customModelData == 2 || itemMeta.customModelData == 3

            if (!isChimarrao)
                return

            // Remove the eaten item (we don't cancel the event because setting to something that doesn't have any eating effects already removes the effects)
            e.setItem(
                when (itemMeta.customModelData) {
                    1 -> CustomItems.CHIMARRAO_EMPTY_BROWN
                    2 -> CustomItems.CHIMARRAO_EMPTY_LORI_WHITE
                    3 -> CustomItems.CHIMARRAO_EMPTY_LORI_BLACK
                    else -> error("Unknown Chimarrão with ID " + itemMeta.customModelData)
                }
            )
            e.player.foodLevel += 6
            e.player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 1_200, 0, false))
        }
    }
}