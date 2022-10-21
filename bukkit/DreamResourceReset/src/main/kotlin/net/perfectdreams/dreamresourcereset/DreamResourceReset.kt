package net.perfectdreams.dreamresourcereset

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamhome.tables.Homes
import net.perfectdreams.dreamresourcereset.listeners.ChunkListener
import net.perfectdreams.dreamresourcereset.listeners.InteractListener
import net.perfectdreams.dreamresourcereset.listeners.PlayerListener
import net.perfectdreams.dreamresourcereset.tables.DeathChestMaps
import net.perfectdreams.dreamresourcereset.tables.DeathChestsInformation
import net.perfectdreams.dreamresourcereset.utils.*
import net.perfectdreams.exposedpowerutils.sql.createOrUpdatePostgreSQLEnum
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapPalette
import org.bukkit.map.MapView
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import javax.imageio.ImageIO

class DreamResourceReset : KotlinPlugin(), Listener {
	companion object {
		val IS_DEATH_CHEST = SparklyNamespacedKey("is_death_chest")
		val DEATH_CHEST_ID = SparklyNamespacedKey("death_chest_id")
	}

	val toBeUsedWorldsFolder = File(dataFolder, "resource_worlds")
	val oldWorldsFolder = File(dataFolder, "old_worlds")
	val cachedInhabitedChunkTimers = mutableMapOf<Long, Long>()

	val stoneImage = ImageIO.read(File(dataFolder, "stone.png"))
	val netherrackImage = ImageIO.read(File(dataFolder, "netherrack.png"))
	val endStoneImage = ImageIO.read(File(dataFolder, "end_stone.png"))

	val lorittaImage = ImageIO.read(File(dataFolder, "loritta.png"))
	val pantufaImage = ImageIO.read(File(dataFolder, "pantufa.png"))
	val powerImage = ImageIO.read(File(dataFolder, "power.png"))
	val gabrielaImage = ImageIO.read(File(dataFolder, "gabriela.png"))
	val sobImage = MapPalette.imageToBytes(ImageIO.read(File(dataFolder, "sob.png")))
	val lorittaAngelImage = MapPalette.imageToBytes(ImageIO.read(File(dataFolder, "loritta_angel.png")))

	val worldAttributesMap = listOf(
		WorldAttributesState.ResourcesWorldAttributesState(this, Bukkit.getServer(), stoneImage),
		WorldAttributesState.NetherWorldAttributesState(this, Bukkit.getServer(), netherrackImage),
		WorldAttributesState.TheEndWorldAttributesState(this, Bukkit.getServer(), endStoneImage)
	).associateBy { it.worldName }
	private val canYouLoseItemsRightNow = mutableMapOf<String, Boolean>()

	override fun softEnable() {
		super.softEnable()

		transaction(Databases.databaseNetwork) {
			createOrUpdatePostgreSQLEnum(DeathChestMapCharacter.values())
			SchemaUtils.createMissingTablesAndColumns(
				DeathChestsInformation,
				DeathChestMaps
			)
		}

		toBeUsedWorldsFolder.mkdirs()
		oldWorldsFolder.mkdirs()

		loadInhabitedChunkTimers()

		registerEvents(PlayerListener(this))
		registerEvents(InteractListener(this))
		registerEvents(ChunkListener(this))

		schedule {
			while (true) {
				for ((worldName, state) in worldAttributesMap) {
					val world = state._world ?: continue
					val previousState = canYouLoseItemsRightNow[worldName] ?: false
					val newState = state.canYouLoseItems()
					if (!previousState && newState) {
						world.players.forEach {
							it.playSound(it.location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f)
						}
					}
					canYouLoseItemsRightNow[worldName] = newState
				}

				for (player in Bukkit.getOnlinePlayers()) {
					val worldAttributes = worldAttributesMap[player.world.name]

					if (worldAttributes?._world != null) {
						val canYouLoseItems = worldAttributes.canYouLoseItems()

						if (canYouLoseItems) {
							player.sendActionBar(
								Component.text("Cuidado! Se você morrer, você perderá seus itens!")
									.color(NamedTextColor.RED)
									.decoration(TextDecoration.BOLD, true)
							)
						} else {
							player.sendActionBar(
								Component.text("Yay! Se você morrer, você não perderá seus itens!")
									.color(NamedTextColor.GREEN)
									.decoration(TextDecoration.BOLD, true)
							)
						}
					}
				}

				waitFor(20L)
			}
		}

		launchMainThread {
			while (true) {
				updateDeathChestMapItems()

				delayTicks(100L) // Every 5 seconds
			}
		}

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
			command("DreamResourceResetMapIdCommand", listOf("dreamrr mapid")) {
				permission = "dreamresourcereset.setup"

				executes {
					val mapId = (player.inventory.itemInMainHand.itemMeta as MapMeta)
						.mapView
						?.id
					val info = transaction(Databases.databaseNetwork) {
						DeathChestsInformation.innerJoin(DeathChestMaps)
							.select {
								DeathChestMaps.id eq mapId
							}.limit(1).firstOrNull()
					}

					if (info != null) {
						player.sendMessage("X: ${info[DeathChestsInformation.x]}")
						player.sendMessage("Y: ${info[DeathChestsInformation.y]}")
						player.sendMessage("Z: ${info[DeathChestsInformation.z]}")
						player.sendMessage("World: ${info[DeathChestsInformation.worldName]}")
					} else {
						player.sendMessage("Map does not exist!")
					}
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
	}

	override fun softDisable() {
		super.softDisable()

		saveInhabitedChunkTimers()

		// TODO: Disable all map renders
	}

	fun initializeDeathChestMap(mapView: MapView) {
		// Check if there is information about this map stored in the database
		launchAsyncThread {
			val info = transaction(Databases.databaseNetwork) {
				DeathChestsInformation.innerJoin(DeathChestMaps)
					.select {
						DeathChestMaps.id eq mapView.id
					}.limit(1).firstOrNull()
			} ?: return@launchAsyncThread // Nope!

			onMainThread {
				updateDeathChestMapItems()

				if (info[DeathChestsInformation.found]) {
					// Remove all renders, this is required to hide all cursor
					mapView.renderers.forEach {
						mapView.removeRenderer(it)
					}

					if (info[DeathChestsInformation.foundBy] == info[DeathChestsInformation.player]) {
						val attributes = worldAttributesMap[mapView.world?.name ?: "Resources"] ?: error("Couldn't find WorldAttributesState for map ${mapView.id}!") // Should NEVER be null!

						mapView.addRenderer(
							DeathChestTombFoundRenderer(
								when (info[DeathChestMaps.character]) {
									DeathChestMapCharacter.LORITTA -> attributes.lorittaDeathChestImage
									DeathChestMapCharacter.PANTUFA -> attributes.pantufaDeathChestImage
									DeathChestMapCharacter.GABRIELA -> attributes.gabrielaDeathChestImage
									DeathChestMapCharacter.POWER -> attributes.powerDeathChestImage
								}
							)
						)
					} else if (info[DeathChestsInformation.gaveBackToUser] == true) {
						mapView.addRenderer(
							DeathChestTombFoundRenderer(
								lorittaAngelImage
							)
						)
					} else {
						mapView.addRenderer(
							DeathChestTombFoundRenderer(
								sobImage
							)
						)
					}
				} else {
					// Remove all map renders EXCEPT the CraftMapRenderer
					mapView.renderers.filter { it::class.simpleName != "CraftMapRenderer" }
						.forEach {
							mapView.removeRenderer(it)
						}

					mapView.addRenderer(
						DeathChestTombTrackRenderer(
							DeathChestInfoData(
								info[DeathChestsInformation.worldName],
								info[DeathChestsInformation.resetVersion],
								info[DeathChestsInformation.x],
								info[DeathChestsInformation.y],
								info[DeathChestsInformation.z],
								info[DeathChestsInformation.found],
							)
						)
					)
				}
			}
		}
	}

	private suspend fun updateDeathChestMapItems() {
		logger.info { "Updating Death Chest Map Items..." }
		DreamUtils.assertMainThread(true)

		// Update death chests maps
		// Sadly we can't just store the Death Chest ID on the map itself, because by the time the object was created in the database, it is already too late
		// So we need to rely on the MapView ID
		val deathChestMaps = mutableListOf<ItemStack>()
		for (player in Bukkit.getOnlinePlayers()) {
			for (item in player.inventory) {
				if (item != null && item.hasItemMeta() && item.type == Material.FILLED_MAP) {
					deathChestMaps.add(item)
				}
			}
		}

		if (deathChestMaps.isNotEmpty()) {
			val mapIdsToBeQueried = deathChestMaps.mapNotNull {
				val itemMeta = it.itemMeta as MapMeta
				itemMeta.mapView?.id ?: return@mapNotNull null
			}
			logger.info { "Querying information about Death Chest Maps $mapIdsToBeQueried" }

			val (infos, playerNames) = onAsyncThread {
				transaction(Databases.databaseNetwork) {
					val infos = DeathChestsInformation
						.innerJoin(DeathChestMaps)
						.select {
							DeathChestMaps.id inList mapIdsToBeQueried
						}.toList()

					val playerNames = Users.select {
						Users.id inList infos.mapNotNull { it[DeathChestsInformation.foundBy]?.value }
					}.toList()

					return@transaction Pair(infos, playerNames)
				}
			}

			for (item in deathChestMaps) {
				val itemMeta = item.itemMeta as MapMeta
				val mapId = itemMeta.mapView?.id ?: continue

				logger.info { "Updating Death Chest Map $item if needed..." }

				val info = infos.firstOrNull { it[DeathChestMaps.id].value == mapId } ?: return

				if (info[DeathChestsInformation.found]) {
					if (info[DeathChestsInformation.player] == info[DeathChestsInformation.foundBy]) {
						item.meta<ItemMeta> {
							displayNameWithoutDecorations("Encontrei o meu Túmulo!") {
								color(NamedTextColor.GOLD)
								decorate(TextDecoration.BOLD)
							}
						}
					} else {
						if (info[DeathChestsInformation.gaveBackToUser] == true) {
							val playerName = playerNames.firstOrNull { info[DeathChestsInformation.foundBy] == it[Users.id] }
								?.get(Users.username)

							item.meta<ItemMeta> {
								displayNameWithoutDecorations {
									color(NamedTextColor.GREEN)
									decorate(TextDecoration.BOLD)

									if (playerName != null) {
										append("Fui salvo pelo ")
										append(playerName) {
											color(NamedTextColor.AQUA)
										}
										append("!")
									} else {
										append("Fui salvo por alguém!")
									}
								}
							}
						} else {
							item.meta<ItemMeta> {
								displayNameWithoutDecorations("Roubaram o meu túmulo!") {
									color(NamedTextColor.RED)
									decorate(TextDecoration.BOLD)
								}
							}
						}
					}
				}
			}
		}
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