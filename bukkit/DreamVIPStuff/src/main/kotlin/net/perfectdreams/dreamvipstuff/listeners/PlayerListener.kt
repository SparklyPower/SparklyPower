package net.perfectdreams.dreamvipstuff.listeners

import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.extensions.rightClick
import net.perfectdreams.dreamvipstuff.DreamVIPStuff
import net.perfectdreams.dreamvipstuff.utils.ExperienceUtils
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

class PlayerListener(val m: DreamVIPStuff) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        val player = e.player

        if (!e.rightClick)
            return

        val clickedBlock = e.clickedBlock ?: return

        if (clickedBlock.type != Material.STONE_BUTTON)
            return

        if (!clickedBlock.location.isWithinRegion("vip_chestxp"))
            return

        val chest = clickedBlock.getRelative(BlockFace.DOWN).state as Chest

        if (8 >= ExperienceUtils.getPlayerExp(e.player)) {
            e.player.sendMessage("${DreamVIPStuff.PREFIX} §cVocê não tem experiência suficiente para transformar sua experiência em potes de XP!")
            return
        }

        val glassBottle = ItemStack(Material.GLASS_BOTTLE)

        if (!chest.inventory.containsAtLeast(glassBottle, 1)) {
            e.player.sendMessage("${DreamVIPStuff.PREFIX} §cVocê precisa colocar potes de vidro dentro do baú para transformá-los em experiência!")
            return
        }

        var idx = 0 // Used to avoid infinite loops
        var count = 0 // Used to show to the player how many bottles they received

        while (ExperienceUtils.getPlayerExp(e.player) >= 8 && chest.inventory.containsAtLeast(glassBottle, 1)) {
            idx++
            if (idx >= 10000) // Who knows, this is to avoid infinite loops, if this ever happens
                break

            val experienceBottle = ItemStack(Material.EXPERIENCE_BOTTLE)

            if (!player.inventory.canHoldItem(experienceBottle))
                break

            chest.inventory.removeItem(ItemStack(Material.GLASS_BOTTLE))
            player.inventory.addItem(experienceBottle)

            player.giveExp(-8)
            count++
        }

        e.player.sendMessage("${DreamVIPStuff.PREFIX} §9$idx potes§a foram convertidos para experiência com sucesso!")
    }

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        m.storedLocations[e.player.uniqueId] = e.player.location // for the /back
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        m.storedLocations.remove(e.player.uniqueId)
    }

    @EventHandler
    fun onRespawn(e: PlayerRespawnEvent) {
        m.storedLocations.remove(e.player.uniqueId)
    }
}