package net.perfectdreams.dreamshopheads.listeners

import net.perfectdreams.dreamcore.utils.balance
import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import org.bukkit.Material
import org.bukkit.block.Skull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class InteractListener : Listener {
    @EventHandler
    fun onRightClick(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return
        if (e.hand != EquipmentSlot.HAND)
            return

        val block = e.clickedBlock

        if (block?.location?.isWithinRegion("players_head_buy_click") == false)
            return

        if (e.clickedBlock?.type != Material.PLAYER_HEAD && e.clickedBlock?.type != Material.PLAYER_WALL_HEAD)
            return

        e.isCancelled = true

        if (500 > e.player.balance) {
            e.player.sendMessage("§8[§9§lLoja§8] §cVocê não tem Sonhos suficientes!")
            return
        }

        val skull = block?.state as Skull

        val newSkull = ItemStack(Material.PLAYER_HEAD)
            .meta<SkullMeta> {
                this.playerProfile = skull.playerProfile
            }

        val playerHead = ItemStack(newSkull)

        if (!e.player.inventory.canHoldItem(playerHead)) {
            e.player.sendMessage("§8[§9§lLoja§8] §cVocê não tem espaço suficiente no seu inventário!")
            return
        }

        e.player.balance -= 500
        e.player.inventory.addItem(playerHead)

        e.player.sendMessage("§8[§9§lLoja§8] §aVocê comprou §9a cabeça§a por §2500 sonhos§a!")
    }
}