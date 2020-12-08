package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreamcustomitems.utils.Microwave
import net.perfectdreams.dreamcustomitems.utils.MicrowaveHolder
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class MicrowaveListener(val m: DreamCustomItems) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onMicrowaveBreak(e: BlockPlaceEvent) {
        val itemInHand = e.itemInHand

        if (itemInHand.type == Material.PLAYER_HEAD && itemInHand.hasItemMeta() && itemInHand.itemMeta.persistentDataContainer.has(CustomItems.IS_MICROWAVE_KEY, PersistentDataType.BYTE)) {
            m.microwaves[e.block.location] = Microwave(m, e.block.location)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onMicrowaveBreak(e: BlockBreakEvent) {
        val clickedBlock = e.block

        if (clickedBlock.type == Material.PLAYER_HEAD || clickedBlock.type == Material.PLAYER_WALL_HEAD) {
            val microwave = m.microwaves[clickedBlock.location] ?: return

            microwave.stop()
            m.microwaves.remove(clickedBlock.location)
            e.isCancelled = true

            e.block.world.dropItemNaturally(
                e.block.location,
                CustomItems.MICROWAVE.clone()
            )

            e.block.type = Material.AIR

            for (i in 3..5) {
                val item = microwave.inventory.getItem(i)

                if (item != null)
                    e.block.world.dropItemNaturally(
                        e.block.location,
                        item
                    )
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onMicrowaveClick(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        val clickedBlock = e.clickedBlock ?: return

        if (clickedBlock.type == Material.PLAYER_HEAD || clickedBlock.type == Material.PLAYER_WALL_HEAD) {
            e.isCancelled = true

            val microwave = m.microwaves[clickedBlock.location] ?: return

            microwave.open(e.player)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryClick(e: InventoryClickEvent) {
        val holder = e.clickedInventory?.holder
        if (holder !is MicrowaveHolder)
            return

        if (e.slot in 3..5) {
            if (holder.m.running) {
                e.isCancelled = true
                holder.m.location.world.spawnParticle(Particle.VILLAGER_ANGRY, holder.m.location, 10, 1.0, 1.0, 1.0)
                e.whoClicked.damage(1.0)
                e.whoClicked.closeInventory()
                e.whoClicked.sendMessage("§cSua mão queimou por você ter achado que seria uma brilhante ideia mexer em uma comida que está no micro-ondas...")
            }
            return
        }

        e.isCancelled = true
        if (e.currentItem?.type == Material.RED_STAINED_GLASS_PANE) {
            holder.m.start()
            return
        }

        if (e.currentItem?.type == Material.GREEN_STAINED_GLASS_PANE) {
            holder.m.stop()
            return
        }
    }
}