package net.perfectdreams.dreammobarena.listeners

import net.perfectdreams.dreammobarena.DreamMobArena
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.inventory.ItemStack

class PlayerListener(val m: DreamMobArena) : Listener {
    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        e.itemsToKeep.clear()
        e.keepInventory = true
        e.setShouldDropExperience(false)

        m.mobArena.removePlayer(e.player)
    }

    @EventHandler
    fun onPvP(e: EntityDamageByEntityEvent) {
        if (e.entityType == EntityType.PLAYER && e.damager.type == EntityType.PLAYER)
            e.isCancelled = true
    }

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {
        e.drops.clear()

        if (e.entity.name == "§c§lPorco de Suprimentos") {
            e.drops.add(ItemStack(Material.COOKED_BEEF))
        }

        m.mobArena.spawnedEntities.remove(e.entity)
    }

    @EventHandler
    fun onCreeperExplode(e: EntityExplodeEvent) {
        e.blockList().clear()
    }

    @EventHandler
    fun onEntitySpawn(e: EntitySpawnEvent) {
        if (e.entity.entitySpawnReason != CreatureSpawnEvent.SpawnReason.CUSTOM)
            e.isCancelled = true
    }
}