package net.perfectdreams.dreamlagstuffrestrictor.listeners

import net.perfectdreams.dreamcore.utils.get
import net.perfectdreams.dreamenderhopper.DreamEnderHopper
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class BlockLaggyBlocksListener : Listener {
    private val maxTypesPerBlockInChunk = mapOf(
        Material.SPAWNER to 4,
        Material.OBSERVER to 64,
        Material.HOPPER to 128
    )

    private val maxMultiTypesPerBlockInChunk = mapOf(
        listOf(
            Material.PISTON,
            Material.STICKY_PISTON
        ) to 64
    )

    @EventHandler(priority = EventPriority.NORMAL)
    fun onSpawn(e: BlockPlaceEvent) {
        val restrictCount = maxTypesPerBlockInChunk[e.block.type]

        if (restrictCount != null) {
            var count = 1

            if (e.itemInHand.hasItemMeta() && e.itemInHand.itemMeta.persistentDataContainer.get(DreamEnderHopper.HOPPER_TELEPORTER))
                return

            for (x in 0..15) {
                for (z in 0..15) {
                    for (y in e.block.world.minHeight until e.block.world.maxHeight) {
                        val block = e.block.chunk.getBlock(x, y, z)
                        if (block.type == e.block.type) {
                            count++

                            if (count > (restrictCount + 1)) {
                                e.isCancelled = true
                                e.player.sendMessage("§cJá existem muitos tipos deste bloco neste chunk! Sim, eu sei que é chato limitar essas coisas, mas tipo... muitos blocos disso em um chunk é a receita para o desastre!")
                                return
                            }
                        }
                    }
                }
            }
        }

        val multiRestrictCount = maxMultiTypesPerBlockInChunk.entries.firstOrNull { e.block.type in it.key }
        if (multiRestrictCount != null) {
            var count = 1

            for (x in 0..15) {
                for (z in 0..15) {
                    for (y in e.block.world.minHeight until e.block.world.maxHeight) {
                        val block = e.block.chunk.getBlock(x, y, z)
                        if (block.type in multiRestrictCount.key) {
                            count++

                            if (count > (multiRestrictCount.value + 1)) {
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