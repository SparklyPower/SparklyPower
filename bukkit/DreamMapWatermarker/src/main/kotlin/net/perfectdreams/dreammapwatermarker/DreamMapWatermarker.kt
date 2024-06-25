package net.perfectdreams.dreammapwatermarker

import com.charleskorn.kaml.Yaml
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.decodeFromString
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammapwatermarker.commands.DreamMapMakerCommand
import net.perfectdreams.dreammapwatermarker.commands.LoriCoolCardsAdminCommand
import net.perfectdreams.dreammapwatermarker.commands.LoriCoolCardsCommand
import net.perfectdreams.dreammapwatermarker.loricoolcards.LoriCoolCardsHandler

import net.perfectdreams.dreammapwatermarker.map.ImgRenderer
import net.perfectdreams.dreammapwatermarker.tables.LoriCoolCardsClaimedAlbums
import net.perfectdreams.dreammapwatermarker.tables.LoriCoolCardsGeneratedMaps
import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class DreamMapWatermarker : KotlinPlugin(), Listener {
	companion object {
		val LOCK_MAP_CRAFT_KEY = SparklyNamespacedKey("lock_map_craft")
		val MAP_CUSTOM_OWNER_KEY = SparklyNamespacedKey("map_custom_owner")

		fun watermarkMap(itemStack: ItemStack, customOwner: UUID?) {
			itemStack.meta<MapMeta> {
				persistentDataContainer.set(LOCK_MAP_CRAFT_KEY, PersistentDataType.BYTE, 1)
				if (customOwner != null)
					persistentDataContainer.set(MAP_CUSTOM_OWNER_KEY, PersistentDataType.STRING, customOwner.toString())
			}
		}
	}

	val imageFolder = File(dataFolder, "img")
	val loriCoolCardsHandler = LoriCoolCardsHandler(this)
	lateinit var config: DreamMapWatermarkerConfig

	override fun softEnable() {
		super.softEnable()
		config = Yaml.default.decodeFromString<DreamMapWatermarkerConfig>(this.getConfig().saveToString())

		imageFolder.mkdirs()

		registerCommand(DreamMapMakerCommand(this))
		if (config.generateLorittaFigurittas) {
			registerCommand(LoriCoolCardsCommand(this))
			registerCommand(LoriCoolCardsAdminCommand(this))
		}

		registerEvents(this)

		transaction(Databases.databaseNetwork) {
			SchemaUtils.create(
				LoriCoolCardsGeneratedMaps,
				LoriCoolCardsClaimedAlbums
			)
		}

		restoreMaps()

		if (config.generateLorittaFigurittas) {
			loriCoolCardsHandler.startLoriCoolCardsMapGenerator()
		}

		registerCommand(
			command("DreamWatermarkMap", listOf("watermarkmap")) {
				permission = "dreamwatermarkmap.watermark"

				executes {
					val playerName = args.getOrNull(0) ?: run {
						player.sendMessage("§cVocê precisa colocar o nome do player!")
						return@executes
					}

					schedule(SynchronizationContext.ASYNC) {
						val uniqueId = DreamUtils.retrieveUserUniqueId(playerName)

						switchContext(SynchronizationContext.SYNC)

						val item = player.inventory.itemInMainHand

						player.inventory.setItemInMainHand(
							item.lore(
								"§7Diretamente da §dGráfica da Pantufa§7...",
								"§7(temos os melhores preços da região!)",
								"§7§oUm incrível mapa para você!",
								"§7",
								"§7Mapa feito para §a${playerName} §e(◠‿◠✿)"
							).apply {
								this.addUnsafeEnchantment(Enchantment.INFINITY, 1)
								this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
								this.meta<ItemMeta> {
									this.persistentDataContainer.set(LOCK_MAP_CRAFT_KEY, PersistentDataType.BYTE, 1)
									this.persistentDataContainer.set(MAP_CUSTOM_OWNER_KEY, PersistentDataType.STRING, uniqueId.toString())
								}
							}
						)
					}
				}
			}
		)
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		val inventoryMatrix = event.inventory.matrix ?: return

		val hasCustomMap = inventoryMatrix.filterNotNull().any {
			it.itemMeta?.persistentDataContainer?.get(MAP_CUSTOM_OWNER_KEY, PersistentDataType.STRING) != null || it.lore?.lastOrNull() == "§a§lObrigado por votar! ^-^" || it.itemMeta?.displayName?.endsWith("Players Online!") == true || it.itemMeta?.persistentDataContainer?.has(LOCK_MAP_CRAFT_KEY, PersistentDataType.BYTE) == true
		}

		if (hasCustomMap)
			event.isCancelled = true
	}

	@EventHandler
	fun onCartography(event: InventoryClickEvent) {
		// We could use "clickedInventory" but that does disallow dragging from the bottom to the top
		val clickedInventory = event.whoClicked.openInventory
		val currentItem = event.currentItem ?: return

		if (clickedInventory.type != InventoryType.CARTOGRAPHY) // el gambiarra
			return

		if (currentItem.itemMeta?.persistentDataContainer?.get(MAP_CUSTOM_OWNER_KEY, PersistentDataType.STRING) != null || currentItem.lore?.lastOrNull() == "§a§lObrigado por votar! ^-^" || currentItem.itemMeta?.displayName?.endsWith("Players Online!") == true || currentItem.itemMeta?.persistentDataContainer?.has(LOCK_MAP_CRAFT_KEY, PersistentDataType.BYTE) == true) {
			event.isCancelled = true
		}

		// Bukkit.broadcastMessage("Moveu item ${event.destination}")
	}

	fun restoreMaps() {
		if (!imageFolder.exists()) {
			logger.warning { "Image Folder does not exist! Skipping map restore process..." }
			return
		}

		// Load the maps in parallel, to speed up server load times
		val jobs = mutableListOf<Job>()
		val semaphore = Semaphore(32)

		for (it in imageFolder.listFiles()) {
			if (it.extension == "png") {
				val mapId = it.nameWithoutExtension.toIntOrNull()
				if (mapId == null) {
					logger.warning { "Invalid Map ID ${it.nameWithoutExtension}! Skipping..." }
					continue
				}

				// We read the map on the main thread to avoid concurrency issues
				// While I don't think I ever had concurrency issues, I already had an issue where some maps just didn't *exist* on the server
				// (as in, no image on the map, using vanilla map renderer)
				// so let's switch this to the main thread just because this call DOES use a non-concurrent safe HashMap
				val mapView = Bukkit.getMap(mapId)
				if (mapView == null) {
					logger.warning { "Map with ID $mapId does not exist! The map must exist/claimed before we are able to restore it! Skipping..." }
					continue
				}

				jobs.add(
					GlobalScope.launch(Dispatchers.IO) {
						semaphore.withPermit {
							val image = ImageIO.read(it)

							mapView.isLocked = true // Optimizes the map because the server doesn't attempt to get the world data when the player is holding the map in their hand
							val renderers: List<MapRenderer> = mapView.renderers

							for (r in renderers) {
								mapView.removeRenderer(r)
							}

							mapView.addRenderer(ImgRenderer(MapPalette.imageToBytes(image)))

							logger.info { "Restored map $mapId!" }
						}
					}
				)
			}
		}

		runBlocking {
			jobs.joinAll()
		}
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	fun toBufferedImage(img: Image): BufferedImage {
		if (img is BufferedImage) {
			return img
		}

		// Create a buffered image with transparency
		val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)

		// Draw the image on to the buffered image
		val bGr = bimage.createGraphics()
		bGr.drawImage(img, 0, 0, null)
		bGr.dispose()

		// Return the buffered image
		return bimage
	}

	// This is just a smol API for other plugins to hook up into DreamMapWatermarker
	suspend fun createImageOnMap(image: BufferedImage): MapView {
		DreamUtils.assertMainThread(true)

		// Create map
		val map = Bukkit.createMap(Bukkit.getWorlds().first { it.name == "world" })

		map.isLocked = true // Optimizes the map because the server doesn't attempt to get the world data when the player is holding the map in their hand
		val renderers: List<MapRenderer> = map.renderers

		for (r in renderers) {
			map.removeRenderer(r)
		}

		map.addRenderer(ImgRenderer(MapPalette.imageToBytes(image)))

		// Save map
		onAsyncThread {
			withContext(Dispatchers.IO) {
				ImageIO.write(image, "png", File(imageFolder, "${map.id}.png"))
			}
		}

		return map
	}
}