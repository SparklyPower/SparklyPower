package net.perfectdreams.dreammobarena

import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack

class MobArena(val m: DreamMobArena) {
    val enemySpawnPoints = mutableListOf<Location>(
        Location(Bukkit.getWorld("world"), 69.5, 90.0, -73.5),
        Location(Bukkit.getWorld("world"), 57.5, 95.0, -105.5),
        Location(Bukkit.getWorld("world"), 57.6, 90.0, -83.5),
    )
    val playerSpawnPoint = Location(Bukkit.getWorld("world"), 93.5, 90.0, -104.5)
    val players = mutableListOf<Player>()
    val spawnedEntities = mutableListOf<Entity>()

    var currentWave = 0
    var running = false

    fun start() {
        currentWave = 0
        running = true

        players.forEach {
            it.teleport(playerSpawnPoint)
            it.inventory.clear()
            it.inventory.addItem(ItemStack(Material.DIAMOND_SWORD))
            it.inventory.setItemInOffHand(ItemStack(Material.SHIELD))
            it.inventory.helmet = ItemStack(Material.IRON_HELMET)
            it.inventory.chestplate = ItemStack(Material.IRON_CHESTPLATE)
            it.inventory.leggings = ItemStack(Material.IRON_LEGGINGS)
            it.inventory.boots = ItemStack(Material.IRON_BOOTS)
        }

        var initialPlayerCount = players.size

        m.launchMainThread {
            delayTicks(5 * 20)

            while (running) {
                currentWave++

                fun spawnEntityAndTargetRandomPlayer(entityType: EntityType): Entity {
                    val spawnPoint = enemySpawnPoints.random()
                    val spawnedEntity = (spawnPoint.world.spawnEntity(spawnPoint, entityType) as Mob)
                    spawnedEntities.add(spawnedEntity)
                    spawnedEntity.target = players.random()
                    return spawnedEntity
                }

                if (currentWave % 2 == 0) {
                    repeat(initialPlayerCount) {
                        (spawnEntityAndTargetRandomPlayer(EntityType.PIG) as Pig)
                            .apply {
                                this.customName = "§c§lPorco de Suprimentos"
                                this.isCustomNameVisible = true
                            }
                    }
                }

                if (currentWave % 4 == 0) {
                    for (player in players) {
                        player.sendMessage("§c§lWAVE $currentWave §e§lESPECIAL§c§l - ${spawnedEntities.size} mobs vivos")
                    }

                    repeat(initialPlayerCount) {
                        repeat(1) {
                            (spawnEntityAndTargetRandomPlayer(EntityType.CREEPER) as Creeper)
                                .isPowered = true
                        }
                        repeat(1) {
                            spawnEntityAndTargetRandomPlayer(EntityType.ZOMBIFIED_PIGLIN)
                        }
                        repeat(1) {
                            spawnEntityAndTargetRandomPlayer(EntityType.BLAZE)
                        }
                    }
                } else {
                    for (player in players) {
                        player.sendMessage("§c§lWAVE $currentWave! §c§l${spawnedEntities.size} mobs vivos")
                    }

                    repeat(initialPlayerCount) {
                        repeat(3) {
                            spawnEntityAndTargetRandomPlayer(EntityType.ZOMBIE)
                        }
                        repeat(2) {
                            spawnEntityAndTargetRandomPlayer(EntityType.SKELETON)
                        }
                        repeat(3) {
                            spawnEntityAndTargetRandomPlayer(EntityType.SPIDER)
                        }
                        repeat(1) {
                            spawnEntityAndTargetRandomPlayer(EntityType.CREEPER)
                        }
                    }
                }

                delayTicks(25 * 20)
            }
        }
    }

    fun removePlayer(player: Player) {
        players.remove(player)

        if (players.isEmpty()) {
            running = false
            Bukkit.broadcastMessage("acabou a mob arena owo")

            spawnedEntities.forEach {
                it.remove()
            }

            spawnedEntities.clear()
        }
    }
}