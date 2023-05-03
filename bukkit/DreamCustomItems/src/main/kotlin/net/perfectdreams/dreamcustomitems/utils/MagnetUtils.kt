package net.perfectdreams.dreamcustomitems.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object MagnetUtils {
    fun getMagnetType(itemStack: ItemStack): MagnetType? {
        if (itemStack.type != Material.STONE_HOE)
            return null

        if (!itemStack.hasItemMeta())
            return null

        if (!itemStack.itemMeta.hasCustomModelData())
            return null

        if (itemStack.itemMeta.customModelData == 1)
            return MagnetType.MAGNET

        if (itemStack.itemMeta.customModelData == 2)
            return MagnetType.WEIRD_MAGNET

        return null
    }

    enum class MagnetType(val requiredAmethystToRepair: Int, val requiredCopperToRepair: Int) {
        MAGNET(16, 16),
        WEIRD_MAGNET(32, 32)
    }

    data class Magnet(
        val itemStack: ItemStack,
        val type: MagnetType
    )
}