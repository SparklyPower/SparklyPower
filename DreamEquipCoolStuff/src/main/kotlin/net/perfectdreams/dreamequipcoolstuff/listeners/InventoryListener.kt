package net.perfectdreams.dreamequipcoolstuff.listeners

import net.perfectdreams.dreamequipcoolstuff.DreamEquipCoolStuff
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class InventoryListener(val m: DreamEquipCoolStuff) : Listener {
    // FROM MASSIVEHAT

    // -------------------------------------------- //
    // CONSTANTS
    // -------------------------------------------- //
    var RAW_HAT_SLOT_ID = 5

    // -------------------------------------------- //
    // LISTENER
    // -------------------------------------------- //
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun hatSwitch(event: InventoryClickEvent) {
        val clicker: HumanEntity = event.whoClicked

        // If a player ...
        if (clicker !is Player) return
        val me: Player = clicker
        val view: InventoryView = event.view

        // ... is clicking around in their own/armor/crafting view ...
        if (view.type !== InventoryType.CRAFTING) return

        // ... and they are clicking their hat slot ...
        if (event.rawSlot != RAW_HAT_SLOT_ID) return
        val cursor: ItemStack? = event.cursor

        if (cursor?.type != Material.WOODEN_HOE)
            return

        val meta = cursor.itemMeta
        val damageable = meta as Damageable

        if (damageable.damage in 1..24) {
            // ... then perform the switch.
            // We deny the normal result
            // NOTE: There is no need to cancel the event since that is just a proxy method for the line below.
            event.result = Event.Result.DENY

            // Schedule swap
            Bukkit.getScheduler().scheduleSyncDelayedTask(m) { // Get
                val current: ItemStack? = event.currentItem

                // Set
                event.currentItem = cursor
                view.cursor = current

                // Update
                clicker.updateInventory()
            }
        }
    }
}