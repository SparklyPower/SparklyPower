package net.perfectdreams.dreamlagstuffrestrictor.listeners

import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent

class BlockMinecartHopperListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onSpawn(e: PlayerInteractEvent) {
        if (e.item?.type == Material.HOPPER_MINECART) {
            e.isCancelled = true

            e.player.sendMessage("§cMinecarts com funis estão desativados no servidor para evitar lag, desculpe pela inconveniência...")
        }
    }

    // Because minecarts can spawn via droppers
    @EventHandler(priority = EventPriority.LOWEST)
    fun onDispense(e: BlockPreDispenseEvent) {
        if (e.block.type == Material.DISPENSER && e.itemStack.type == Material.HOPPER_MINECART)
            e.isCancelled = true
    }

    // I made this to avoid droppers dispensing minecarts, but it didn't seem to work...
    @EventHandler(priority = EventPriority.LOWEST)
    fun onSpawn(e: EntitySpawnEvent) {
        if (e.entityType == EntityType.MINECART_HOPPER)
            e.isCancelled = true
    }
}