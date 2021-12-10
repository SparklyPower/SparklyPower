package net.perfectdreams.dreammochilas.utils

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class MochilaInventoryHolder : InventoryHolder {
    val accessHolders: Queue<MochilaAccessHolder> = LinkedList()

    override fun getInventory(): Inventory {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}