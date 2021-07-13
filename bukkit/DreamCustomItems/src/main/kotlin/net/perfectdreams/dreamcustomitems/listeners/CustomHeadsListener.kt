package net.perfectdreams.dreamcustomitems.listeners

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.holders.CustomItemRecipeHolder
import net.perfectdreams.dreamcustomitems.holders.MicrowaveHolder
import net.perfectdreams.dreamcustomitems.holders.SuperFurnaceHolder
import net.perfectdreams.dreamcustomitems.items.Microwave
import net.perfectdreams.dreamcustomitems.items.SuperFurnace
import net.perfectdreams.dreamcustomitems.items.TrashCan
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class CustomHeadsListener(val m: DreamCustomItems) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCustomItemPlace(e: BlockPlaceEvent) {
        val itemInHand = e.itemInHand

        if (itemInHand.type == Material.PLAYER_HEAD && itemInHand.hasItemMeta() && itemInHand.itemMeta.persistentDataContainer.has(CustomItems.IS_MICROWAVE_KEY, PersistentDataType.BYTE)) {
            m.microwaves[e.block.location] = Microwave(m, e.block.location)
        }

        if (itemInHand.type == Material.PLAYER_HEAD && itemInHand.hasItemMeta() && itemInHand.itemMeta.persistentDataContainer.has(CustomItems.IS_SUPERFURNACE_KEY, PersistentDataType.BYTE)) {
            m.superfurnaces[e.block.location] = SuperFurnace(m, e.block.location)
        }

        if (itemInHand.type == Material.PLAYER_HEAD && itemInHand.hasItemMeta() && itemInHand.itemMeta.persistentDataContainer.has(CustomItems.IS_TRASHCAN_KEY, PersistentDataType.BYTE)) {
            m.trashcans[e.block.location] = TrashCan(m, e.block.location)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCustomItemBreak(e: BlockBreakEvent) {
        val clickedBlock = e.block

        if (clickedBlock.type == Material.PLAYER_HEAD || clickedBlock.type == Material.PLAYER_WALL_HEAD) {
            when {
                m.microwaves[clickedBlock.location] != null -> {
                    val microwave = m.microwaves[clickedBlock.location] ?: return

                    microwave.stop()

                    // Close inventory to avoid dupes when breaking a custom block while someone has the block's inventory open
                    microwave.inventory.viewers.forEach {
                        it.closeInventory()
                    }

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

                m.superfurnaces[clickedBlock.location] != null -> {
                    val superfurnace = m.superfurnaces[clickedBlock.location] ?: return
                    superfurnace.stop()

                    // Close inventory to avoid dupes when breaking a custom block while someone has the block's inventory open
                    superfurnace.inventory.viewers.forEach {
                        it.closeInventory()
                    }

                    m.superfurnaces.remove(clickedBlock.location)
                    e.isCancelled = true

                    e.block.world.dropItemNaturally(
                            e.block.location,
                            CustomItems.SUPERFURNACE.clone()
                    )

                    e.block.type = Material.AIR

                    listOf(0, 1, 2, 3, 4, 5, 18, 19, 20, 21, 22, 23, 27,28, 29, 30, 31, 32).forEach { i ->
                        val item = superfurnace.inventory.getItem(i)
                        if (item != null)
                            e.block.world.dropItemNaturally(
                                    e.block.location,
                                    item
                            )

                    }
                }

                m.trashcans[clickedBlock.location] != null -> {
                    val watercontainer = m.trashcans[clickedBlock.location] ?: return

                    m.trashcans.remove(clickedBlock.location)

                    e.isCancelled = true
                    e.block.world.dropItemNaturally(
                            e.block.location,
                            CustomItems.TRASHCAN.clone()
                    )

                    e.block.type = Material.AIR
                }

                else -> return
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCustomItemClick(e: PlayerInteractEvent) {
        if (!e.rightClick)
            return

        val clickedBlock = e.clickedBlock ?: return

        if (clickedBlock.type == Material.PLAYER_HEAD || clickedBlock.type == Material.PLAYER_WALL_HEAD) {
            e.isCancelled = true

            val claim = GriefPrevention.instance.dataStore.getClaimAt(clickedBlock.location, false, null)

            if (claim != null && (claim.ownerName != e.player.name && claim.allowContainers(e.player) != null)) {
                e.player.sendMessage("§cVocê não tem permissão para mexer neste item!")
                return
            }

            when {
                m.microwaves[clickedBlock.location] != null -> {
                    val microwave = m.microwaves[clickedBlock.location] ?: return

                    microwave.open(e.player)
                }

                m.superfurnaces[clickedBlock.location] != null -> {
                    val superfurnace = m.superfurnaces[clickedBlock.location] ?: return

                    if (!e.player.hasPermission("group.vip") && !e.player.hasPermission("group.vip+") && !e.player.hasPermission("group.vip++")) {
                        e.player.sendMessage("§cVocê não tem permissão para usar essa ferramenta, apenas §b§lVIPs§c!")
						return
                    }

                    superfurnace.open(e.player)
                }

                m.trashcans[clickedBlock.location] != null -> {
                    val trashCan = m.trashcans[clickedBlock.location] ?: return

                    trashCan.open(e.player)
                }

                else -> return
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryClick(e: InventoryClickEvent) {
        val holder = e.clickedInventory?.holder

        when(holder) {
            is MicrowaveHolder -> {
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

            is SuperFurnaceHolder -> {
                if (e.slot in 0..5 || e.slot in 18..23 || e.slot in 27..32) {
                    if (holder.m.running) {
                        e.isCancelled = true
                        holder.m.location.world.spawnParticle(Particle.VILLAGER_ANGRY, holder.m.location, 10, 1.0, 1.0, 1.0)
                        e.whoClicked.damage(2.5*2)
                        e.whoClicked.closeInventory()
                        e.whoClicked.sendMessage("§cSua mão queimou por você ter achado que seria uma brilhante ideia mexer em uma super fornalha em funcionamento...")
                    }
                    return
                }

                e.isCancelled = true

                if (e.currentItem?.type == Material.RED_STAINED_GLASS_PANE) {
                    holder.m.start(e.whoClicked as Player)
                    return
                }

                if (e.currentItem?.type == Material.GREEN_STAINED_GLASS_PANE) {
                    holder.m.stop()
                    return
                }
            }

            is CustomItemRecipeHolder -> {
                e.isCancelled = true
            }

            else -> return
        }
    }
}
