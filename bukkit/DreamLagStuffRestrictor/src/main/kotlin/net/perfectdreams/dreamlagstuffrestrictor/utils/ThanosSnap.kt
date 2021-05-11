package net.perfectdreams.dreamlagstuffrestrictor.utils

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamlagstuffrestrictor.DreamLagStuffRestrictor
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent

class ThanosSnap(val m: DreamLagStuffRestrictor) : Listener {
    fun start() {
        m.registerEvents(this)

        m.schedule {
            while (true) {
                // if (false) {
                var killedMobs = mutableMapOf<EntityType, Int>()

                for (player in Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }) {
                    player.sendMessage("Rodando o Thanos Snap...")
                }

                for (player in Bukkit.getOnlinePlayers().filter { it.world.name == "world" }) {
                    val nearby = player.getNearbyEntities(16.0, 16.0, 16.0)

                    fun checkMobNearby(type: EntityType) {
                        killedMobs[type] = killedMobs.getOrPut(type, { 0 }) + theSnap(nearby, type)
                    }

                    checkMobNearby(EntityType.VILLAGER)
                    checkMobNearby(EntityType.CHICKEN)
                    checkMobNearby(EntityType.CREEPER)
                    checkMobNearby(EntityType.ZOMBIE)
                    checkMobNearby(EntityType.SKELETON)
                    checkMobNearby(EntityType.SPIDER)
                    checkMobNearby(EntityType.CAVE_SPIDER)
                    checkMobNearby(EntityType.BLAZE)
                    checkMobNearby(EntityType.PIG)
                    checkMobNearby(EntityType.SHEEP)
                    checkMobNearby(EntityType.TURTLE)
                    checkMobNearby(EntityType.COW)
                    checkMobNearby(EntityType.GHAST)
                    checkMobNearby(EntityType.PILLAGER)
                    // checkMobNearby(EntityType.PIG_ZOMBIE)
                    checkMobNearby(EntityType.RABBIT)
                    checkMobNearby(EntityType.FOX)
                    checkMobNearby(EntityType.BEE)
                    checkMobNearby(EntityType.STRIDER)

                    waitFor(10)
                }

                val total = killedMobs.values.sumBy { it }

                if (total != 0) {
                    // Bukkit.broadcastMessage("§cO §dThanos Snap™§c passou e levou §4${total} mobs diferentes§c, sendo eles...")

                    fun announceIfNeeded(type: EntityType, text: String) {
                        val total = killedMobs[type]!!

                        if (total != 0) {
                            // Bukkit.broadcastMessage("§4✝ §c$total $text...")
                        }
                    }

                    announceIfNeeded(EntityType.VILLAGER, "aldeões indefesos")
                    announceIfNeeded(EntityType.CHICKEN, "galinhas tagarelas")
                    announceIfNeeded(EntityType.CREEPER, "creepers, aw man")
                    announceIfNeeded(EntityType.ZOMBIE, "zumbis salafrários")
                    announceIfNeeded(EntityType.SKELETON, "esqueletos magros")
                    announceIfNeeded(EntityType.SPIDER, "aranhas chatas")
                    announceIfNeeded(EntityType.CAVE_SPIDER, "aranhas de caverna chatas")
                    announceIfNeeded(EntityType.PIG, "porcos sujos")
                    announceIfNeeded(EntityType.SHEEP, "ovelhas marotas")
                    announceIfNeeded(EntityType.TURTLE, "tartarugas lentas")
                    announceIfNeeded(EntityType.COW, "vacas gordas")
                    announceIfNeeded(EntityType.BLAZE, "blazes voadores")
                    announceIfNeeded(EntityType.GHAST, "ghasts gigantes")
                    announceIfNeeded(EntityType.PILLAGER, "pillagers violentos")
                    // announceIfNeeded(EntityType.PIG_ZOMBIE, "pigmans mutantes")
                    announceIfNeeded(EntityType.RABBIT, "coelhos saltitantes")
                    announceIfNeeded(EntityType.FOX, "raposas travessas")
                    announceIfNeeded(EntityType.BEE, "abelhas zum zum zum")
                    announceIfNeeded(EntityType.STRIDER, "striders marotos")

                    // Bukkit.broadcastMessage("§cTema. Tente fugir. O §dThanos Snap™§c sempre chegará... (Por favor, evite farms para evitar lag no servidor! thx!!)")
                }
                // }

                waitFor(20 * 30)
            }
        }
    }

    val limitEntities = mutableListOf(
        EntityType.ZOMBIE,
        EntityType.CREEPER,
        EntityType.CAVE_SPIDER,
        EntityType.SPIDER,
        EntityType.SKELETON,
        EntityType.PILLAGER,
        EntityType.VILLAGER,
        EntityType.FOX,
        EntityType.PIG,
        EntityType.SHEEP,
        EntityType.CHICKEN,
        EntityType.TURTLE,
        EntityType.COW,
        EntityType.BLAZE,
        EntityType.STRIDER
    )

    val maximumAround = mapOf(
        EntityType.ZOMBIE to 30,
        EntityType.SPIDER to 30,
        EntityType.CAVE_SPIDER to 30,
        EntityType.CREEPER to 30,
        EntityType.SKELETON to 30,
        EntityType.BLAZE to 30,
        EntityType.CHICKEN to 15,
        EntityType.SHEEP to 15,
        EntityType.PIG to 15,
        EntityType.COW to 15
    )

    val maximumInChunks = mapOf(
        EntityType.ZOMBIE to 30,
        EntityType.SPIDER to 30,
        EntityType.CAVE_SPIDER to 30,
        EntityType.CREEPER to 30,
        EntityType.SKELETON to 30,
        EntityType.BLAZE to 30,
        EntityType.CHICKEN to 15,
        EntityType.SHEEP to 15,
        EntityType.PIG to 15,
        EntityType.COW to 15,
        EntityType.STRIDER to 15
    )

    @EventHandler
    fun onSpawn(e: org.bukkit.event.vehicle.VehicleCreateEvent) {
        if (e.vehicle.type == EntityType.MINECART_HOPPER) {
            val nearby = e.vehicle.getNearbyEntities(0.001, 0.001, 0.001)
                .filter { it.type == e.vehicle.type }

            Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }.forEach {
                it.sendMessage("§ceta §6${e.vehicle.type}§c nasceu em §9(${e.vehicle.location.x}, ${e.vehicle.location.y}, ${e.vehicle.location.z})§c, tem §e${nearby.size} iguais§c no mesmo bloco, iremos matar os demais")
            }

            nearby.forEach {
                it.remove()
            }
        }
    }

    @EventHandler
    fun onSpawn(e: EntitySpawnEvent) {
        if (e.location.world.name != "world")
            return

        if (e.entity.type in limitEntities) {
            val entityTypeInChunk = e.location.chunk.entities.filter { it.type == e.entity.type }
            val entityTypeInChunkCount = entityTypeInChunk.size
            val maxInChunks = maximumInChunks[e.entity.type] ?: 10

            if (entityTypeInChunkCount > maxInChunks) {
                e.isCancelled = true
                Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }.forEach {
                    // it.sendMessage("§6${e.entity.type}§c foi #cancelado em §9(${e.entity.location.x}, ${e.entity.location.y}, ${e.entity.location.z})§c, tem §e$entityTypeInChunkCount §6${e.entity.type}§e no chunk")
                }

                entityTypeInChunk.drop(maxInChunks).forEach {
                    var hasName = false

                    if (it.customName != null && it.customName != "") {
                        hasName = true
                    }

                    if (!hasName && it is org.bukkit.entity.Damageable) {
                        it.damage(100000.0)
                        Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }.forEach { player ->
                            // player.sendMessage("§6${it.type}§c foi morto em §9(${it.location.x}, ${it.location.y}, ${it.location.z})§c por ter muitos mobs no chunk (mais que 7)")
                        }
                    }
                }
            }
        }
    }

    fun theSnap(entities: List<Entity>, whoWillBeKilled: EntityType): Int {
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

            val maximumAround = maximumAround[whoWillBeKilled] ?: 20

            if (!hasName && idx > maximumAround && it is org.bukkit.entity.Damageable) {
                it.damage(100000.0)
                dead++
            }

            idx++
        }

        return dead
    }
}