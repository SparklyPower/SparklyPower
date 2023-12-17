package net.perfectdreams.dreamcorreios.listeners

import kotlinx.coroutines.delay
import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
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
import org.bukkit.inventory.ItemStack
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
            if (caixaPostal.items.isEmpty()) {
                e.player.sendMessage("§aSua caixa postal está vazia!")
            } else {
                onMainThread {
                    val itemsThatCouldNotFit = mutableListOf<ItemStack>()

                    caixaPostal.items.forEach { itemStack ->
                        if (e.player.inventory.canHoldItem(itemStack)) {
                            e.player.inventory.addItem(itemStack)
                        } else {
                            itemsThatCouldNotFit.add(itemStack)
                        }
                    }

                    caixaPostal.items.clear()
                    if (itemsThatCouldNotFit.isEmpty()) {
                        e.player.sendMessage("§aVocê esvaziou a sua caixa postal!")
                    } else {
                        caixaPostal.items.addAll(itemsThatCouldNotFit)
                        e.player.sendMessage("§aAinda tem ${itemsThatCouldNotFit.size} itens na sua caixa postal que não foram dados pois o seu inventário está cheio!")
                    }
                }
            }
            caixaPostal.release()
        }
    }
}