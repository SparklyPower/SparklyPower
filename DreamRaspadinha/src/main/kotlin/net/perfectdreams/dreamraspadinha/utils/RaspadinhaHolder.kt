package net.perfectdreams.dreamraspadinha.utils

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class RaspadinhaHolder(val raspadinhaId: Long) : InventoryHolder {
    var scratchedCount = 0

    override fun getInventory(): Inventory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}