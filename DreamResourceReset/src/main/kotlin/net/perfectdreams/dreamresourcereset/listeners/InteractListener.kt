package net.perfectdreams.dreamresourcereset.listeners

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.LocationUtils
import net.perfectdreams.dreamcore.utils.extensions.*
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamresourcereset.DreamResourceReset
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class InteractListener(val m: DreamResourceReset) : Listener {
    @EventHandler
    fun onTorchInteract(e: PlayerInteractEvent) {
        if (!e.rightClick && !e.leftClick)
            return

        val item = e.player.inventory.itemInMainHand

        if (item.type != Material.REDSTONE_TORCH)
            return

        if (item.hasStoredMetadataWithKey("quickTeleport")) {
            e.setUseItemInHand(Event.Result.DENY)

            val playerLocation = e.player.location

            if (playerLocation.world.name != "world" && playerLocation.world.name != "Resources") {
                e.player.sendMessage("§cVocê não pode usar a tocha de teletransporte rápido neste mundo!")
                return
            }

            val storeInMetaKey = if (playerLocation.world.name == "world")
                "previousWorldLocation"
            else "previousResourcesLocation"

            val loadFromMetaKey = if (playerLocation.world.name != "world")
                "previousWorldLocation"
            else "previousResourcesLocation"

            e.player.inventory.setItemInMainHand(
                item.storeMetadata(
                    storeInMetaKey,
                    "${playerLocation.world.name};${playerLocation.x};${playerLocation.y};${playerLocation.z};${playerLocation.yaw};${playerLocation.pitch}"
                )
            )

            val storedLocation = item.getStoredMetadata(loadFromMetaKey) ?: run {
                if (loadFromMetaKey == "previousWorldLocation") {
                    Bukkit.dispatchCommand(e.player, "spawn")
                } else {
                    Bukkit.dispatchCommand(e.player, "warp recursos")
                }
                return
            }

            val resourceWorldChange = item.getStoredMetadata("resourceWorldChange")?.toInt() ?: 0

            if (loadFromMetaKey == "previousResourcesLocation" && resourceWorldChange != m.config.getInt("resourceWorldChange", 0)) {
                Bukkit.dispatchCommand(e.player, "warp recursos")
                e.player.sendMessage("§cO mundo de recursos foi regenerado desde a última vez que você apareceu por lá!")
                e.player.inventory.setItemInMainHand(
                    item.storeMetadata(
                        "resourceWorldChange",
                        m.config.getInt("resourceWorldChange").toString()
                    )
                )
                return
            }

            val split = storedLocation.split(";")

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

            e.player.removePotionEffect(PotionEffectType.INVISIBILITY)

            val world = Bukkit.getWorld("Resources")

            var location: Location?

            while (true) {
                try {
                    val x = DreamUtils.random.nextInt(-2500, 2500)
                    val z = DreamUtils.random.nextInt(-2500, 2500)
                    val highestY = world?.getHighestBlockYAt(x, z) ?: 0
                    location = Location(world, x.toDouble(), highestY.toDouble(), z.toDouble())
                        .getSafeDestination()
                    break
                } catch (e: LocationUtils.HoleInFloorException) {
                    waitFor(5L)
                }
            }

            if (location == null)
                return@schedule

            e.player.sendTitle("§bWoosh!", "", 0, 20, 10)
            e.player.teleport(location) // Teletransportar player
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