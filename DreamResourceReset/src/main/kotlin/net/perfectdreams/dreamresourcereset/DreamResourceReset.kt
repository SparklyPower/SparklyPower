package net.perfectdreams.dreamresourcereset

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreamhome.tables.Homes
import net.perfectdreams.dreamresourcereset.listeners.InteractListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class DreamResourceReset : KotlinPlugin(), Listener {
	val toBeUsedWorldsFolder = File(dataFolder, "resource_worlds")
	val oldWorldsFolder = File(dataFolder, "old_worlds")

	override fun softEnable() {
		super.softEnable()

		toBeUsedWorldsFolder.mkdirs()
		oldWorldsFolder.mkdirs()

		registerEvents(InteractListener(this))

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
	}

	override fun softDisable() {
		super.softDisable()
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
			config.set("resourceWorldChange", config.getInt("resourceWorldChange", 0))
			saveConfig()

			logger.info("Done! Resource world changed!")
		}
	}
}