package net.perfectdreams.dreamlagstuffrestrictor.utils

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamlagstuffrestrictor.DreamLagStuffRestrictor
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.vehicle.VehicleCreateEvent
import org.bukkit.inventory.ItemStack

class ThanosSnap(val m: DreamLagStuffRestrictor) : Listener {
    fun start() {
        m.registerEvents(this)

        m.schedule {
            while (true) {
                // if (false) {
                var killedMobs = mutableMapOf<EntityType, Int>()

                for (player in Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }) {
                    player.sendMessage("\ue251 §x§e§6§b§2§e§8Rodando o Thanos Snap...")
                }

                for (player in Bukkit.getOnlinePlayers().filter { it.world.name == "world" }) {
                    val currentChunk = player.chunk

                    fun checkMobNearby(type: EntityType) {
                        killedMobs[type] = killedMobs.getOrPut(type, { 0 }) + theSnap(currentChunk, type)
                    }

                    for ((type, _) in maximumInChunks) {
                        checkMobNearby(type)
                    }

                    waitFor(1)
                }

                val total = killedMobs.values.sumBy { it }

                if (total != 0) {
                    for (player in Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }) {
                        player.sendMessage("\uE251 §cO §dThanos Snap™§c passou e levou §4${total} mobs diferentes§c, sendo eles...")
                    }

                    fun announceIfNeeded(type: EntityType) {
                        val total = killedMobs[type]!!

                        if (total != 0) {
                            for (player in Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }) {
                                player.sendMessage("\uE251 §4✝ §c$total $type...")
                            }
                        }
                    }

                    for ((type, _) in maximumInChunks) {
                        announceIfNeeded(type)
                    }

                    // Bukkit.broadcastMessage("§cTema. Tente fugir. O §dThanos Snap™§c sempre chegará... (Por favor, evite farms para evitar lag no servidor! thx!!)")
                } else {
                    for (player in Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }) {
                        player.sendMessage("\uE251 §x§e§6§b§2§e§8Thanos Snap não limpou nenhum mob...")
                    }
                }
                // }

                waitFor(20 * 30)
            }
        }
    }

    val maximumInChunks = mapOf(
        EntityType.ZOMBIE to 30,
        EntityType.SPIDER to 30,
        EntityType.CAVE_SPIDER to 30,
        EntityType.CREEPER to 30,
        EntityType.SKELETON to 30,
        EntityType.BLAZE to 30,
        EntityType.SNOWBALL to 30,
        EntityType.EGG to 30,
        EntityType.CHICKEN to 15,
        EntityType.SHEEP to 15,
        EntityType.PIG to 15,
        EntityType.COW to 15,
        EntityType.MUSHROOM_COW to 15,
        EntityType.STRIDER to 15,
        EntityType.ZOMBIFIED_PIGLIN to 15,
        EntityType.FOX to 15,
        EntityType.FROG to 15,
        EntityType.TADPOLE to 15,
        EntityType.GOAT to 15,
        EntityType.TURTLE to 15,
        EntityType.RABBIT to 15,
        EntityType.BEE to 15,
        EntityType.PILLAGER to 10,
        EntityType.VILLAGER to 10,
        EntityType.MINECART_HOPPER to 16,
    )

    @EventHandler
    fun onSpawn(e: EntitySpawnEvent) {
        if (e.location.world.name != "world")
            return

        val maxInChunks = maximumInChunks[e.entity.type]
        if (maxInChunks != null) {
            val entityTypeInChunk = e.location.chunk.entities.filter { it.type == e.entity.type }
            val entityTypeInChunkCount = entityTypeInChunk.size

            if (entityTypeInChunkCount > maxInChunks) {
                e.isCancelled = true
            }
        }
    }

    fun theSnap(chunk: Chunk, whoWillBeKilled: EntityType): Int {
        val entities = chunk.entities
        val entitiesNearby = entities.filter { it.type == whoWillBeKilled }

        var idx = 0
        var dead = 0
        entitiesNearby.forEach {
            var hasName = false

            if (it.customName != null && it.customName != "") {
                hasName = true
            }

            if (whoWillBeKilled == EntityType.VILLAGER)
                hasName = false

            val maximumAround = maximumInChunks[whoWillBeKilled] ?: 10

            if (!hasName && idx > maximumAround) {
                if (it is org.bukkit.entity.Damageable)
                    it.damage(100000.0)
                else
                    it.remove()

                if (it is org.bukkit.entity.Minecart) {
                    chunk.world.dropItem(it.location, ItemStack(it.minecartMaterial))
                }

                dead++
            }

            idx++
        }

        return dead
    }
}