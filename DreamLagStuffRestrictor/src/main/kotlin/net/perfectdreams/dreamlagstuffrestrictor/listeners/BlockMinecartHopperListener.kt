package net.perfectdreams.dreamlagstuffrestrictor.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class BlockMinecartHopperListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onSpawn(e: PlayerInteractEvent) {
        if (e.item?.type == Material.HOPPER_MINECART) {
            e.isCancelled = true

            e.player.sendMessage("§cMinecarts com funis estão desativados no servidor para evitar lag, desculpe pela inconveniência...")
        }
    }
}