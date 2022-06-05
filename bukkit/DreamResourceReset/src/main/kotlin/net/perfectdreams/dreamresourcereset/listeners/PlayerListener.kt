package net.perfectdreams.dreamresourcereset.listeners

import net.perfectdreams.dreamcore.utils.extensions.canBreakAt
import net.perfectdreams.dreamcore.utils.extensions.getSafeDestination
import net.perfectdreams.dreamresourcereset.DreamResourceReset
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class PlayerListener(val m: DreamResourceReset) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDeath(e: PlayerDeathEvent) {
        val attributes = m.worldAttributesMap[e.player.world.name] ?: return
        val canYouLoseItems = attributes.canYouLoseItems()

        // Get where the chest should be placed
        val whereThePlayerDied = e.player.location
        val whereTheBlockShouldBePlaced = whereThePlayerDied.getSafeDestination()
            .block

        if (!canYouLoseItems || !e.player.canBreakAt(whereTheBlockShouldBePlaced.location, Material.CHEST)) {
            e.keepInventory = true
            e.keepLevel = true

            // The documentation says that you need to clear the drops and clear the dropped XP if you set to keep inventory/level
            e.drops.clear()
            e.droppedExp = 0
        } else {
            e.keepInventory = false
            e.keepLevel = false

            whereTheBlockShouldBePlaced.type = Material.CHEST
            val state = whereTheBlockShouldBePlaced.state as Chest
            state.persistentDataContainer.set(
                DreamResourceReset.IS_DEATH_CHEST,
                PersistentDataType.BYTE,
                1
            )
            state.persistentDataContainer.set(
                DreamResourceReset.DEATH_DROPPED_XP,
                PersistentDataType.INTEGER,
                e.droppedExp
            )
            state.update()

            for (item in e.drops) {
                state.blockInventory.addItem(item)
            }

            e.drops.clear()
            e.droppedExp = 0
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDeathChestInteract(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return
        if (clickedBlock.type != Material.CHEST)
            return

        val state = clickedBlock.state as Chest
        if (state.persistentDataContainer.get(DreamResourceReset.IS_DEATH_CHEST, PersistentDataType.BYTE) != 1.toByte())
            return

        val deathDroppedXp = state.persistentDataContainer.getOrDefault(DreamResourceReset.DEATH_DROPPED_XP, PersistentDataType.INTEGER, 0)
        e.player.giveExp(deathDroppedXp)
        e.player.playSound(e.player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.7f)

        state.persistentDataContainer.remove(DreamResourceReset.IS_DEATH_CHEST)
        state.persistentDataContainer.remove(DreamResourceReset.DEATH_DROPPED_XP)
        state.update()
    }
}