package net.perfectdreams.dreamresourcereset.listeners

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamresourcereset.DreamResourceReset
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class InteractListener(val m: DreamResourceReset) : Listener {
    companion object {
        private const val FIVE_MINUTES_IN_TICKS = (5 * 60) * 20
        private val QUICK_RESOURCES_TELEPORT_TORCH_KEY = SparklyNamespacedKey("is_quick_resources_teleport")
        private val QUICK_RESOURCES_TELEPORT_PREVIOUS_WORLD_LOCATION_KEY = SparklyNamespacedKey("quick_resources_teleport_world_location")
        private val QUICK_RESOURCES_TELEPORT_PREVIOUS_RESOURCES_LOCATION_KEY = SparklyNamespacedKey("quick_resources_teleport_resources_location")
        private val QUICK_RESOURCES_TELEPORT_LAST_RESET_KEY = SparklyNamespacedKey("quick_resources_teleport_last_reset")
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onTorchInteract(e: PlayerInteractEvent) {
        if (!e.rightClick && !e.leftClick)
            return

        val item = e.player.inventory.itemInMainHand

        if (item.type != Material.REDSTONE_TORCH)
            return

        // TODO: Remove this hack after the "/kit noob" is updated with the new metadata key
        if (item.hasItemMeta() && !item.itemMeta.persistentDataContainer.has(QUICK_RESOURCES_TELEPORT_TORCH_KEY, PersistentDataType.BYTE) && item.hasStoredMetadataWithKey("quickTeleport")) {
            item.meta<ItemMeta> {
                persistentDataContainer.set(
                    QUICK_RESOURCES_TELEPORT_TORCH_KEY,
                    PersistentDataType.BYTE,
                    1
                )
            }
            return onTorchInteract(e)
        }

        if (item.hasItemMeta() && item.itemMeta.persistentDataContainer.has(QUICK_RESOURCES_TELEPORT_TORCH_KEY, PersistentDataType.BYTE)) {
            e.setUseItemInHand(Event.Result.DENY)

            val itemMeta = item.itemMeta
            val persistentDataContainer = itemMeta.persistentDataContainer
            val playerLocation = e.player.location

            if (playerLocation.world.name != "world" && playerLocation.world.name != "Survival2" && playerLocation.world.name != "Resources") {
                e.player.sendMessage("§cVocê não pode usar a tocha de teletransporte rápido neste mundo!")
                return
            }

            if (playerLocation.world.name == "Resources") {
                // User is in resources world
                persistentDataContainer.set(
                    QUICK_RESOURCES_TELEPORT_PREVIOUS_RESOURCES_LOCATION_KEY,
                    PersistentDataType.STRING,
                    "${playerLocation.world.name};${playerLocation.x};${playerLocation.y};${playerLocation.z};${playerLocation.yaw};${playerLocation.pitch}"
                )

                persistentDataContainer.set(
                    QUICK_RESOURCES_TELEPORT_LAST_RESET_KEY,
                    PersistentDataType.INTEGER,
                    m.config.getInt(
                        "resourceWorldChange",
                        0
                    )
                )

                val previousWorldLocation = persistentDataContainer.get(QUICK_RESOURCES_TELEPORT_PREVIOUS_WORLD_LOCATION_KEY, PersistentDataType.STRING)
                if (previousWorldLocation != null) {
                    val split = previousWorldLocation.split(";")
                    e.player.teleport(
                        Location(
                            Bukkit.getWorld(split[0])!!,
                            split[1].toDouble(),
                            split[2].toDouble(),
                            split[3].toDouble(),
                            split[4].toFloat(),
                            split[5].toFloat()
                        )
                    )
                } else {
                    Bukkit.dispatchCommand(e.player, "spawn")
                }
            } else if (playerLocation.world.name in Constants.SPARKLYPOWER_SURVIVAL_WORLDS) {
                // User is in Survival world
                persistentDataContainer.set(
                    QUICK_RESOURCES_TELEPORT_PREVIOUS_WORLD_LOCATION_KEY,
                    PersistentDataType.STRING,
                    "${playerLocation.world.name};${playerLocation.x};${playerLocation.y};${playerLocation.z};${playerLocation.yaw};${playerLocation.pitch}"
                )

                val previousResourcesLocation = persistentDataContainer.get(QUICK_RESOURCES_TELEPORT_PREVIOUS_RESOURCES_LOCATION_KEY, PersistentDataType.STRING)
                val hasBeenResetSinceTheLastTimeWeWentToTheResourcesWorld = persistentDataContainer.get(QUICK_RESOURCES_TELEPORT_LAST_RESET_KEY, PersistentDataType.INTEGER) !=  m.config.getInt(
                    "resourceWorldChange",
                    0
                )

                if (previousResourcesLocation != null && !hasBeenResetSinceTheLastTimeWeWentToTheResourcesWorld) {
                    val split = previousResourcesLocation.split(";")
                    e.player.teleport(
                        Location(
                            Bukkit.getWorld(split[0])!!,
                            split[1].toDouble(),
                            split[2].toDouble(),
                            split[3].toDouble(),
                            split[4].toFloat(),
                            split[5].toFloat()
                        )
                    )
                } else {
                    Bukkit.dispatchCommand(e.player, "warp recursos")

                    if (hasBeenResetSinceTheLastTimeWeWentToTheResourcesWorld) {
                        e.player.sendMessage("§cO mundo de recursos foi regenerado desde a última vez que você apareceu por lá!")
                    }
                }
            } else {
                // Unknown location
                e.player.sendMessage("§cVocê está em um mundo onde a tocha de teletransporte rápido não funciona!")
                return
            }

            item.itemMeta = itemMeta
        }
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.action != Action.PHYSICAL)
            return

        val clickedBlock = e.clickedBlock
        if (clickedBlock?.type != Material.HEAVY_WEIGHTED_PRESSURE_PLATE)
            return

        val block = clickedBlock.getRelative(BlockFace.DOWN, 2)

        if (block.type != Material.OAK_SIGN && block.type != Material.OAK_WALL_SIGN)
            return

        val sign = block.state as Sign
        if (sign.getLine(0) != "[SparklyRR]")
            return

        if (!e.player.location.worldGuardRegions.any { it.id.startsWith("resources_spawn") })
            return

        if (e.player.hasPotionEffect(PotionEffectType.INVISIBILITY))
            return

        e.player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1200, 0))

        scheduler().schedule(m) {
            // Pra que delay? Porque, se não tem, o console mostra "Moved wrongly!" e o player não muda de lugar
            // bad bad
            waitFor(5L)

            val world = Bukkit.getWorld("Resources")!!

            var location: Location?

            // To avoid users getting into chunks that have too much "Player activity", we are going to get chunks with less
            // inhabited timer than other chunks
            // shl = convert chunk pos to block pos
            // shr = convert block pos to chunk pos

            var chunksChecked = 0
            while (true) {
                try {
                    // https://stackoverflow.com/questions/5837572/generate-a-random-point-within-a-circle-uniformly
                    val r = 2500 * sqrt(DreamUtils.random.nextDouble())
                    val theta = DreamUtils.random.nextDouble() * 2 * Math.PI
                    val x = (r * cos(theta)).toInt()
                    val z = (r * sin(theta)).toInt()

                    val chunkX = x shr 4
                    val chunkY = z shr 4

                    val inhabitedTimerInChunk = m.getInhabitedChunkTimerInResourcesWorldAt(chunkX, chunkY)

                    m.logger.info { "Trying to use chunk ($x; $z = $chunkX, $chunkY); Inhabited timer in chunk is $inhabitedTimerInChunk" }

                    // We are going to max check 60 chunks
                    val bypassChecks = chunksChecked == 60

                    if (!bypassChecks && inhabitedTimerInChunk >= FIVE_MINUTES_IN_TICKS) {
                        m.logger.info { "Skipping Chunk ($x, $z) due to too much activeness! $inhabitedTimerInChunk >= $FIVE_MINUTES_IN_TICKS" }
                        waitFor(1L)
                        chunksChecked++
                        continue
                    }

                    val chunk = world.getChunkAt(chunkX, chunkY)

                    if (!bypassChecks) {
                        // If there is any players in the current chunk, skip it
                        if (chunk.entities.any { it is Player }) {
                            m.logger.info { "Skipping Chunk ($x, $z) because there is another player in the same chunk!" }
                            waitFor(1L)
                            chunksChecked++
                            continue
                        }

                        val randomBlockInChunk = chunk.getBlock(0, 0, 0)
                        if (randomBlockInChunk.biome == Biome.OCEAN || randomBlockInChunk.biome.name.endsWith("_OCEAN")) {
                            m.logger.info { "Skipping Chunk ($x, $z) because it is an ocean!" }
                            waitFor(1L)
                            chunksChecked++
                            continue
                        }
                    }

                    val highestY = world.getHighestBlockYAt(x, z)
                    location = Location(world, x.toDouble(), highestY.toDouble(), z.toDouble())
                        .getSafeDestination()
                    break
                } catch (e: LocationUtils.HoleInFloorException) {
                    waitFor(1L)
                    chunksChecked++
                }
            }

            if (location == null) {
                e.player.removePotionEffect(PotionEffectType.INVISIBILITY)
                return@schedule
            }

            e.player.sendTitle("§bWoosh!", "", 0, 20, 10)
            e.player.teleport(location) // Teletransportar player
            e.player.removePotionEffect(PotionEffectType.INVISIBILITY)
            e.player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 600, 1)) // Efeito de velocidade
            e.player.addPotionEffect(
                PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE,
                    600,
                    3
                )
            ) // Efeito de anti dano (para evitar mortes)
        }
    }
}