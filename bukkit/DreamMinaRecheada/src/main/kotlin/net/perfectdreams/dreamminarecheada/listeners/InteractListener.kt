package net.perfectdreams.dreamminarecheada.listeners

import net.perfectdreams.dreamcore.utils.WorldGuardUtils
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamminarecheada.DreamMinaRecheada
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class InteractListener(internal val m: DreamMinaRecheada) : Listener {
    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        if (e.entity.world.name == "MinaRecheada") {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        if (e.to.world.name == "MinaRecheada") {
            m.minaRecheada.bossBar1?.addPlayer(e.player)
            m.minaRecheada.bossBar2?.addPlayer(e.player)

            var hasPickaxe = false
            val storageContents = e.player.inventory.storageContents
            if (storageContents != null) {
                for (itemStack in storageContents) {
                    if (itemStack != null && itemStack.type.name.contains("PICKAXE")) {
                        hasPickaxe = true
                        break
                    }
                }
            }
            if (!hasPickaxe) {
                val player = e.player
                val firstEmpty = player.inventory.firstEmpty()
                if (firstEmpty != -1) {
                    player.sendMessage(DreamMinaRecheada.PREFIX + "§aNão é seguro ficar aqui sem uma picareta! Aqui, pegue isto!")

                    player.inventory.setItem(firstEmpty, player.inventory.itemInMainHand)
                    val ironPickaxe = ItemStack(Material.IRON_PICKAXE, 1)
                    ironPickaxe.meta<ItemMeta> {
                        this.setDisplayName("§fPicareta da §e§lMina §6§lR§e§le§6§lc§e§lh§6§le§e§la§6§ld§e§la§8")
                        this.lore = listOf("§7Para pobres que não possuem uma picareta!")
                    }

                    val itemMeta = ironPickaxe.itemMeta
                    itemMeta.isUnbreakable = true
                    ironPickaxe.itemMeta = itemMeta
                    player.inventory.setItemInMainHand(ironPickaxe)
                }
            }
        }
    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        if (e.player.hasPermission("sparklyminarecheada.bypass"))
            return

        if (e.player.world.name == "MinaRecheada") {
            if (WorldGuardUtils.isWithinRegion(e.block.location, "minarecheada")) {
                val type = e.block.type

                if (type == Material.QUARTZ_BLOCK || type == Material.QUARTZ_PILLAR || type == Material.CHISELED_QUARTZ_BLOCK || type == Material.LADDER) {
                    e.isCancelled = true
                    return
                }
            } else {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.player.world.name != "MinaRecheada") {
            val itemStack = e.item
            if (itemStack != null && itemStack.type == Material.IRON_PICKAXE) {
                if (itemStack.hasItemMeta() && itemStack.itemMeta.hasLore()) {
                    if (itemStack.itemMeta.lore?.getOrNull(0) == "§7Para pobres que não possuem uma picareta!") {
                        e.isCancelled = true
                        e.player.sendMessage(DreamMinaRecheada.PREFIX + "§cEsta picareta é peculiar... ela possui um poder que apenas permite você utiliza-lá na Mina Recheada!")
                    }
                }
            }
        }
    }
}
