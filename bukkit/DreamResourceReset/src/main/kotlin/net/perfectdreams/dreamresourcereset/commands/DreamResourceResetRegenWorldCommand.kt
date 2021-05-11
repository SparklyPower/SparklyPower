package net.perfectdreams.dreamresourcereset.commands

import kotlinx.coroutines.delay
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamresourcereset.DreamResourceReset
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import java.io.File

object DreamResourceResetRegenWorldCommand : DSLCommandBase<DreamResourceReset> {
    override fun command(plugin: DreamResourceReset) = create(
        listOf("dreamrr regen")
    ) {
        permission = "dreamresourcereset.setup"

        executes {
            sender.sendMessage("Regenerating World...")

            plugin.launchMainThread {
                // Avoid any issues
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill cancel")

                delayTicks(100L)

                // Unload the current world
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvremove Resources")

                onAsyncThread {
                    // Delete the folder
                    File("Resources").deleteRecursively()
                }

                // Create new world
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvcreate Resources normal")

                // Wait 20 ticks
                delayTicks(20L)

                // Set the world
                // Needs to be lowercase if not "Invalid value for [] (Not a valid world: Resources), acceptable values are any world" ???
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/world resources")

                // Load the schematic
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/schem load warp_resources_spawn_point_flat")

                // Wait 100 ticks because it seems it loads async
                delayTicks(100L)

                val ySpawn = 68

                // Set the coordinates
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos1 0,$ySpawn,0")

                // Paste the schematic
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/paste")

                // Set resources spawn
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvsetspawn Resources 0.5 68 0.5")

                // Improve area around the spawn point to be accessible by players
                val world = Bukkit.getWorld("Resources")!!

                val northBoundCoordinates = (-18..18).map {
                    SpawnBlockCheck(BlockFace.NORTH, world, it, ySpawn - 1, -19)
                }

                val westBoundCoordinates = (-18..18).map {
                    SpawnBlockCheck(BlockFace.WEST, world, -19, ySpawn - 1, it)
                }

                val southBoundCoordinates = (-18..18).map {
                    SpawnBlockCheck(BlockFace.SOUTH, world, it, ySpawn - 1, 19)
                }

                val eastBoundCoordinates = (-18..18).map {
                    SpawnBlockCheck(BlockFace.EAST, world, 19, ySpawn - 1, it)
                }

                northBoundCoordinates.forEach {
                    it.check()
                }

                westBoundCoordinates.forEach {
                    it.check()
                }

                southBoundCoordinates.forEach {
                    it.check()
                }

                eastBoundCoordinates.forEach {
                    it.check()
                }

                // Save all because WorldBorder fails if this isn't set
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all")

                // Again wait for 100 ticks because save all takes a while
                delayTicks(100L)

                // Start generating the world
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb Resources fill 10000")

                // Let's go!!!
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "wb fill confirm")
            }
        }
    }

    data class SpawnBlockCheck(val direction: BlockFace, val world: World, var x: Int, var y: Int, var z: Int) {
        fun check() {
            // Does the player have any space to walk around here?
            val blockAtPlayerFeet = world.getBlockAt(x, y, z)
            val blockAtPlayerHead = world.getBlockAt(x, y + 1, z)
            val blockAtPlayerHeadPlusOne = world.getBlockAt(x, y + 2, z)
            val blockBelowPlayerFeet = world.getBlockAt(x, y - 1, z)

            if (blockAtPlayerFeet.type == Material.AIR && blockAtPlayerHead.type == Material.AIR && blockAtPlayerHeadPlusOne.type == Material.AIR) {
                // Also check if we aren't floating...
                if (!blockBelowPlayerFeet.isSolid) {
                    // oof feelings only, time to go down!
                    blockBelowPlayerFeet.type = Material.GRASS_BLOCK
                    val relative = blockAtPlayerFeet.getRelative(direction)
                    x = relative.x
                    z = relative.z
                    y -= 1
                    check() // And check again
                    return
                }

                if (blockBelowPlayerFeet.type == Material.WATER) {
                    // If it is water, let's replace with a grass block to avoid the player swimming for too long
                    blockBelowPlayerFeet.type = Material.GRASS_BLOCK
                    val relative = blockAtPlayerFeet.getRelative(direction)
                    x = relative.x
                    z = relative.z
                    check() // And check again
                    return
                }

                // Everything seems to be fine here :)
                println("Finished! $direction $x, $y, $z")
            } else {
                // Not everything seems to be fine... The player can't fit in here! Time to go up!
                for (y in y..world.maxHeight) {
                    // Set all blocks until max height to air
                    world.getBlockAt(x, y, z)
                        .type = Material.AIR
                }

                blockBelowPlayerFeet.type = Material.GRASS_BLOCK

                val relative = blockAtPlayerFeet.getRelative(direction)
                x = relative.x
                z = relative.z
                y += 1
                check() // And check again!
                return
            }
        }
    }
}