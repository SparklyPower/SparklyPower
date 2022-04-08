package net.perfectdreams.dreamxizum.battle

import net.perfectdreams.dreamxizum.dao.Kit
import org.bukkit.inventory.ItemStack

class BattleOptions {
    var items = mutableListOf<ItemStack>()
        set(value) { field = mutableListOf<ItemStack>().apply { addAll(value) } }
    var armor = mutableSetOf<ItemStack>()
        set(value) { field = mutableSetOf<ItemStack>().apply { addAll(value) } }

    var itemsType = BattleItems.CUSTOM_ITEMS
    var allowMcMMO = false
    var dropHeads = false
    var legacyPvp = true
    var timeLimit = 3

    var sonecas = 0.0
        set(value) { field = if (value < 0) 0.0 else value }
    var cash = 0L
        set(value) { field = if (value < 0) 0L else value }

    fun clearItems() {
        items.clear()
        armor.clear()
    }

    fun loadKit(kit: Kit) {
        items = kit.buildItems()
        armor = kit.buildArmor()
    }

    override fun hashCode() = (items + armor).hashCode()
    override fun equals(other: Any?) = hashCode() == other.hashCode()
}