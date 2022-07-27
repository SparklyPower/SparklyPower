package net.perfectdreams.dreamcorreios.listeners

import kotlinx.coroutines.delay
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcorreios.DreamCorreios
import net.perfectdreams.dreamcorreios.utils.CaixaPostalHolder
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class CaixaPostalListener(val m: DreamCorreios) : Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onInteract(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return

        if (clickedBlock.type != Material.CHEST)
            return

        val state = clickedBlock.state as Chest
        if (state.persistentDataContainer.get(DreamCorreios.IS_CAIXA_POSTAL, PersistentDataType.BYTE) != 1.toByte())
            return

        e.isCancelled = true
        
        m.launchAsyncThread {
            val caixaPostal = m.retrieveCaixaPostalOfPlayerAndHold(e.player)
            val inventory = m.createCaixaPostalInventoryOfPlayer(e.player, caixaPostal, 0)

            onMainThread {
                e.player.openInventory(inventory)
            }
        }
    }

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        // TODO: Improve this
        val holder = e.inventory.holder
        if (holder !is CaixaPostalHolder)
            return

        val player = e.whoClicked as? Player ?: return

        val clickedInventory = e.clickedInventory ?: return

        if (clickedInventory.holder !is CaixaPostalHolder && e.action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // Block SHIFT click from inventory -> custom GUI
            e.isCancelled = true
        }

        if (clickedInventory.holder is CaixaPostalHolder) {
            // Items cannot be put inside a mailbox!
            if (e.action.name.startsWith("PLACE_")) {
                e.isCancelled = true
                return
            }

            if (e.slot in DreamCorreios.SKIPPED_SLOTS) {
                e.isCancelled = true

                // While we DO have access to the caixa postal items, it is better to retrieve it again to ensure proper synchronization
                if (e.slot == 8) {
                    // Close the inventory to ensure that the changes will be persisted, to avoid issues
                    // THIS IS NECESSARY TO AVOID A DUPLICATION ISSUE WHEN YOU TRY OPENING THE SAME PAGE WHILE THE PAGE IS OPEN
                    e.whoClicked.closeInventory()

                    // Open the mailbox on the next page, if possible
                    m.launchAsyncThread {
                        val caixaPostal = m.retrieveCaixaPostalOfPlayerAndHold(player)

                        val inventory = m.createCaixaPostalInventoryOfPlayer(player, caixaPostal, holder.page - 1)

                        onMainThread {
                            player.openInventory(inventory)
                        }
                    }
                }

                if (e.slot == 17) {
                    // Close the inventory to ensure that the changes will be persisted, to avoid issues
                    // THIS IS NECESSARY TO AVOID A DUPLICATION ISSUE WHEN YOU TRY OPENING THE SAME PAGE WHILE THE PAGE IS OPEN
                    e.whoClicked.closeInventory()

                    // Open the mailbox on the next page, if possible
                    m.launchAsyncThread {
                        val caixaPostal = m.retrieveCaixaPostalOfPlayerAndHold(player)

                        val inventory = m.createCaixaPostalInventoryOfPlayer(player, caixaPostal, holder.page + 1)

                        onMainThread {
                            player.openInventory(inventory)
                        }
                    }
                }

                if (e.slot == 53) {
                    e.whoClicked.closeInventory()
                }
                return
            }
        }
    }

    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        val holder = e.inventory.holder
        if (holder !is CaixaPostalHolder)
            return

        val currentPage = holder.itemsPerPages[holder.page]
        currentPage.clear()

        val items = (0 until e.inventory.size).filter { it !in DreamCorreios.SKIPPED_SLOTS }.map {
            e.inventory.getItem(it)
        }.filterNotNull()
        currentPage.addAll(items)

        holder.caixaPostalAccessHolder.items.clear()
        holder.caixaPostalAccessHolder.items.addAll(holder.itemsPerPages.flatten())

        m.launchAsyncThread {
            holder.caixaPostalAccessHolder.release()
        }
    }
}