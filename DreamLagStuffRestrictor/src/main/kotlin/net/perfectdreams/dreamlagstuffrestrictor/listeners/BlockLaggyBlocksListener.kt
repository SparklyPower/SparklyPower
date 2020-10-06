package net.perfectdreams.dreamlagstuffrestrictor.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

class BlockLaggyBlocksListener : Listener {
    val maxTypesPerBlockInChunk = mapOf(
        Material.SPAWNER to 8,
        Material.OBSERVER to 64,
        Material.PISTON to 32,
        Material.STICKY_PISTON to 32
    )

    @EventHandler(priority = EventPriority.NORMAL)
    fun onSpawn(e: BlockPlaceEvent) {
        val restrictCount = maxTypesPerBlockInChunk[e.block.type]

        if (restrictCount != null) {
            var count = 1

            for (x in 0..15) {
                for (z in 0..15) {
                    for (y in 0..255) {
                        val block = e.block.chunk.getBlock(x, y, z)
                        if (block.type == e.block.type) {
                            count++

                            if (count >= restrictCount) {
                                e.isCancelled = true
                                e.player.sendMessage("§cJá existem muitos tipos deste bloco neste chunk! Sim, eu sei que é chato limitar essas coisas, mas tipo... muitos blocos disso em um chunk é a receita para o desastre!")
                                return
                            }
                        }
                    }
                }
            }
        }
    }
}