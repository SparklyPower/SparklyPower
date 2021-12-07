package net.perfectdreams.dreamresourcereset

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamhome.tables.Homes
import net.perfectdreams.dreamresourcereset.commands.DreamResourceResetRegenWorldCommand
import net.perfectdreams.dreamresourcereset.listeners.ChunkListener
import net.perfectdreams.dreamresourcereset.listeners.InteractListener
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.lang.RuntimeException

class DreamResourceReset : KotlinPlugin(), Listener {
	val toBeUsedWorldsFolder = File(dataFolder, "resource_worlds")
	val oldWorldsFolder = File(dataFolder, "old_worlds")
	val cachedInhabitedChunkTimers = mutableMapOf<Long, Long>()

	override fun softEnable() {
		super.softEnable()

		toBeUsedWorldsFolder.mkdirs()
		oldWorldsFolder.mkdirs()

		loadInhabitedChunkTimers()

		registerEvents(InteractListener(this))
		registerEvents(ChunkListener(this))

		schedule {
			while (true) {
				val world = Bukkit.getWorld("Resources")

				if (world != null) {
					for (chunk in world.loadedChunks) {
						cachedInhabitedChunkTimers[chunk.chunkKey] = chunk.inhabitedTime
					}
				}

				waitFor(1200L)
			}
		}

		registerCommand(
			command("DreamResourceResetCommand", listOf("dreamrr")) {
				permission = "dreamresourcereset.setup"

				executes {
					player.inventory.addItem(
						ItemStack(Material.REDSTONE_TORCH)
							.rename("§c§lTeletransporte Rápido")
							.storeMetadata("quickTeleport", "true")
					)
				}
			}
		)

		registerCommand(
			command("DreamResourceResetChangeCommand", listOf("dreamrr change")) {
				permission = "dreamresourcereset.setup"

				executes {
					changeResourceWorld()
				}
			}
		)

		registerCommand(
			command("DreamResourceResetChangeCommand", listOf("dreamrr save")) {
				permission = "dreamresourcereset.setup"

				executes {
					saveInhabitedChunkTimers()
				}
			}
		)

		registerCommand(
			command("DreamResourceResetChangeCommand", listOf("dreamrr inhabitated")) {
				permission = "dreamresourcereset.setup"

				executes {
					player.sendMessage("Inhabited From Cache: ${getInhabitedChunkTimerInResourcesWorldAt(player.location.blockX shr 4, player.location.blockZ shr 4)}")
					player.sendMessage("Inhabited From Chunk: ${player.chunk.inhabitedTime}")
				}
			}
		)

		registerCommand(DreamResourceResetRegenWorldCommand)
	}

	override fun softDisable() {
		super.softDisable()

		saveInhabitedChunkTimers()
	}

	fun changeResourceWorld() {
		schedule {
			val resourcesWorldFolder = File("Resources")

			logger.info("Unloading resources world...")
			// Unloading the world...
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvunload Resources")

			logger.info("Moving old world to the old worlds folder...")
			switchContext(SynchronizationContext.ASYNC)
			File("Resources").renameTo(File(oldWorldsFolder, "Resources-${System.currentTimeMillis()}"))

			logger.info("Getting a random world from the worlds folder...")
			val worldToBeUsed = toBeUsedWorldsFolder.listFiles().filter { it.isDirectory }.random()
			logger.info("We are going to use ${worldToBeUsed}!")

			logger.info("Copying the world folder...")

			File("Resources").mkdirs()

			worldToBeUsed.copyRecursively(resourcesWorldFolder, true)

			switchContext(SynchronizationContext.SYNC)
			logger.info("Clearing cached inhabited chunk timers...")
			cachedInhabitedChunkTimers.clear()
			saveInhabitedChunkTimers()

			logger.info("Loading the new world...")
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mvload Resources")

			logger.info("Deleting homes in the resources world...")
			switchContext(SynchronizationContext.ASYNC)
			transaction(Databases.databaseNetwork) {
				Homes.deleteWhere { Homes.worldName eq "Resources" }
			}

			switchContext(SynchronizationContext.SYNC)
			logger.info("Reloading DreamWarps...")
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plugman reload DreamWarps")

			logger.info("Increase resource world change count...")
			config.set("resourceWorldChange", config.getInt("resourceWorldChange", 0) + 1)
			saveConfig()

			logger.info("Done! Resource world changed!")
		}
	}

	fun loadInhabitedChunkTimers() {
		if (config.contains("inhabitedChunkTimers")) {
			val inhabitedChunkTimers = config.getConfigurationSection("inhabitedChunkTimers")?.getValues(false)
				?.map {
					val value = it.value
					it.key.toLong() to if (value is Long)
						value
					else if (value is Int)
						value.toLong()
					else throw RuntimeException("$value is not a Long or a Int!")
				}?.toMap()

			if (inhabitedChunkTimers != null) {
				cachedInhabitedChunkTimers.putAll(inhabitedChunkTimers)
				logger.info { "Loaded ${cachedInhabitedChunkTimers.size} inhabited chunk timers!" }
			}
		}
	}

	fun saveInhabitedChunkTimers() {
		// No need to save inhabitated timers with 0
		config.set("inhabitedChunkTimers", cachedInhabitedChunkTimers.filterValues { it != 0L })
		saveConfig()
	}

	/**
	 * Gets the inhabited chunk timer from the cache or, if it wasn't ever saved in the cache, null
	 */
	fun getInhabitedChunkTimerInResourcesWorldAt(x: Int, z: Int): Long {
		val world = Bukkit.getWorld("Resources")!!

		if (world.isChunkLoaded(x, z))
			// If the chunk is loaded, load the inhabited time from memory
			return world.getChunkAt(x, z).inhabitedTime

		// If not, load it from the cache
		return cachedInhabitedChunkTimers.getOrDefault(Chunk.getChunkKey(x, z), 0L)
	}
}