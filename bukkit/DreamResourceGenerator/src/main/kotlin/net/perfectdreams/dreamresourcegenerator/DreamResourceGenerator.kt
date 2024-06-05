package net.perfectdreams.dreamresourcegenerator

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import org.popcraft.chunky.api.ChunkyAPI
import java.io.File

class DreamResourceGenerator : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()
		if (DreamCore.dreamConfig.serverName != "SparklyPower Survival Generator Resources") {
			// Let's avoid a BIG disaster, shall we?
			error("Running DreamResourceGenerator in a server that isn't named \"SparklyPower Survival Generator Resources\", erroring out to avoid a big disaster...")
		}

		registerEvents(this)
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onServerLoad(e: ServerLoadEvent) {
		logger.info { "Starting Resources World Generation..." }
		val chunkyAPI = Bukkit.getServer().servicesManager.load(ChunkyAPI::class.java)
		if (chunkyAPI == null) {
			logger.warning { "Chunky is not available..." }
			return
		}

		launchMainThread {
			regenerateWorld(chunkyAPI)
		}
	}

	private suspend fun regenerateWorld(chunkyAPI: ChunkyAPI) {
		// Avoid any issues
		chunkyAPI.cancelTask("Resources")

		delayTicks(100L)

		// Unload the current world
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvremove Resources")

		onAsyncThread {
			// Delete the folder
			File("Resources").deleteRecursively()
		}

		// Create new world
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvcreate Resources normal")

		var resourcesWorld: World?
		while (true) {
			resourcesWorld = Bukkit.getWorld("Resources")
			if (resourcesWorld != null)
				break

			logger.info("World is not present, waiting until the world is present...")
			delayTicks(20L)
		}

		logger.info("World has been found! ${resourcesWorld!!} - Saving world...")

		resourcesWorld.save()

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

		// Disable Keep Inventory
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv gamerule keepInventory false Resources")

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

		Bukkit.getWorld("Resources")!!.save()

		while (true) {
			if (File("Resources/region").listFiles().isNotEmpty())
				break

			logger.info("Region files are not present, trying again later in 20 ticks...")
			delayTicks(20L)
		}

		// Set the vanilla world border
		resourcesWorld.worldBorder.setCenter(0.0, 0.0)
		resourcesWorld.worldBorder.size = 10000.0

		// Start generating the world
		val started = chunkyAPI.startTask(
			"Resources",
			"square",
			0.0,
			0.0,
			5000.0,
			5000.0,
			"concentric"
		)

		logger.info { "Is the world generation process started? $started" }

		chunkyAPI.onGenerationComplete {
			logger.info { "Finished World Generation! Saving Resources world..." }

			launchMainThread {
				Bukkit.getWorld("Resources")!!.save()

				logger.info { "Removing Resources world from Multiverse-Core..." }

				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvremove Resources")

				delayTicks(100L)

				logger.info { "Copying Resources world to /srv/survival_worlds/Resources..." }

				File("Resources").copyRecursively(File("/srv/survival_worlds/Resources"))

				logger.info { "Creating file indicating that the Resources world is ready to be used..." }
				File("/srv/survival_worlds/Resources.ready").writeText("ready! uwu")

				delayTicks(100L)

				logger.info { "Finished! Shutting down server..." }

				Bukkit.shutdown()
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