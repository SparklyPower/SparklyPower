package net.perfectdreams.dreamcustomitems

import com.comphenix.protocol.ProtocolLibrary
import com.google.common.collect.Sets
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcustomitems.blocks.BlockPacketAdapter
import net.perfectdreams.dreamcustomitems.commands.CustomItemRecipeCommand
import net.perfectdreams.dreamcustomitems.commands.CustomItemsCommand
import net.perfectdreams.dreamcustomitems.items.Microwave
import net.perfectdreams.dreamcustomitems.items.SuperFurnace
import net.perfectdreams.dreamcustomitems.items.TrashCan
import net.perfectdreams.dreamcustomitems.listeners.*
import net.perfectdreams.dreamcustomitems.utils.BlockPosition
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.*
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DreamCustomItems : KotlinPlugin(), Listener {
	private val recipes = mutableListOf<NamespacedKey>()
	val microwaves = mutableMapOf<Location, Microwave>()
	val superfurnaces = mutableMapOf<Location, SuperFurnace>()
	val trashcans = mutableMapOf<Location, TrashCan>()

	val microwavesDataFile by lazy {
		File(dataFolder, "microwaves.yml")
	}

	val superfurnacesDataFile by lazy {
		File(dataFolder, "superfurnaces.yml")
	}

	val trashcansDataFile by lazy {
		File(dataFolder, "trashcans.yml")
	}

	val microwavesData by lazy {
		writeDataFile(microwavesDataFile)

		YamlConfiguration.loadConfiguration(microwavesDataFile)
	}

	val superfurnacesData by lazy {
		writeDataFile(superfurnacesDataFile)

		YamlConfiguration.loadConfiguration(superfurnacesDataFile)
	}

	val trashcansData by lazy {
		writeDataFile(trashcansDataFile)

		YamlConfiguration.loadConfiguration(trashcansDataFile)
	}

	// Custom Blocks in Worlds
	val customBlocksInWorlds = ConcurrentHashMap<String, MutableSet<BlockPosition>>()

	val customBlocksFolder = File(dataFolder, "custom_blocks")
	fun getCustomBlocksInWorld(worldName: String) = customBlocksInWorlds.getOrPut(worldName) { Sets.newConcurrentHashSet() }

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()
		customBlocksFolder.mkdirs()

		loadAllMicrowaves()
		loadAllSuperFurnaces()
		loadAllTrashCans()
		loadAllCustomBlocks()

		schedule {
			while (true) {
				waitFor(20 * (15 * 60)) // every 15m
				saveAllMicrowaves()
				saveAllSuperFurnaces()
				saveAllTrashCans()
				saveAllCustomBlocks()
			}
		}

		registerEvents(BlockListener(this))
		registerEvents(CustomHeadsListener(this))
		registerEvents(BlockCraftListener(this))
		registerEvents(RubyDropListener(this))
		registerEvents(CustomBlocksListener(this))
		registerEvents(EstalinhoListener(this))

		val protocolManager = ProtocolLibrary.getProtocolManager()
		protocolManager.addPacketListener(BlockPacketAdapter(this))

		registerCommand(CustomItemsCommand)
		registerCommand(CustomItemRecipeCommand)

		addRecipe(
			"hamburger",
			CustomItems.HAMBURGER,
			listOf(
				"BBB",
				"SSS",
				"BBB"
			)
		) {
			it.setIngredient('B', Material.BREAD)
			it.setIngredient('S', Material.COOKED_BEEF)
		}

		addRecipe(
			"cupcake",
			CustomItems.CUPCAKE,
			listOf(
				" M ",
				"SES",
				" T "
			)
		) {
			it.setIngredient('M', Material.MILK_BUCKET)
			it.setIngredient('S', Material.SUGAR)
			it.setIngredient('E', Material.EGG)
			it.setIngredient('T', Material.WHEAT)
		}

		addRecipe(
			"microwave",
			CustomItems.MICROWAVE,
			listOf(
				"IIR",
				"PPU",
				"IIR"
			)
		) {
			it.setIngredient('I', Material.IRON_INGOT)
			it.setIngredient('P', Material.GLASS_PANE)
			it.setIngredient('R', Material.REDSTONE)
			// TODO: Filter
			it.setIngredient('U', Material.PRISMARINE_SHARD)
		}

		addRecipe(
			"superfurnace",
			CustomItems.SUPERFURNACE,
			listOf(
				"UNU",
				"EBE",
				"UNU"
			)
		) {
			it.setIngredient('U', Material.PRISMARINE_SHARD)
			it.setIngredient('B', Material.BLAST_FURNACE)
			it.setIngredient('N', Material.NETHER_STAR)
			it.setIngredient('E', Material.EMERALD_BLOCK)
		}

		addRecipe(
			"trashcan",
			CustomItems.TRASHCAN,
			listOf(
				"I I",
				"IXI",
				"III"
			)
		) {
			it.setIngredient('I', Material.IRON_INGOT)
			it.setIngredient('X', Material.PAPER)
		}

		addRecipe(
			"rainbow_wool",
			CustomItems.RAINBOW_WOOL,
			listOf(
				"RWR",
				"WUW",
				"RWR"
			)
		) {
			it.setIngredient('W', Material.WHITE_WOOL)
			it.setIngredient('R', Material.REDSTONE_BLOCK)
			// TODO: Filter
			it.setIngredient('U', Material.PRISMARINE_SHARD)
		}

		addRecipe(
			"estalinho_red",
			CustomItems.ESTALINHO_RED,
			listOf(
				"CPC",
				"PXP",
				"CPC"
			)
		) {
			it.setIngredient('C', Material.RED_DYE)
			it.setIngredient('P', Material.PAPER)
			it.setIngredient('X', Material.GUNPOWDER)
		}

		addRecipe(
			"estalinho_green",
			CustomItems.ESTALINHO_GREEN,
			listOf(
				"CPC",
				"PXP",
				"CPC"
			)
		) {
			it.setIngredient('C', Material.GREEN_DYE)
			it.setIngredient('P', Material.PAPER)
			it.setIngredient('X', Material.GUNPOWDER)
		}
	}

	override fun softDisable() {
		super.softDisable()

		saveAllMicrowaves()
		saveAllSuperFurnaces()
		saveAllTrashCans()
		saveAllCustomBlocks()

		recipes.forEach {
			Bukkit.removeRecipe(it)
		}
	}

	fun loadAllMicrowaves() {
		if (microwavesData.contains("microwaves")) {
			val list = microwavesData.get("microwaves", null) as List<Map<String, Any?>>

			for (entry in list) {
				val location = entry["location"] as Location
				val items = entry["items"] as List<ItemStack>

				microwaves[location] = Microwave(this, location).apply {
					for ((index, item) in items.withIndex()) {
						inventory.setItem(
							3 + index,
							item
						)
					}
				}
			}
		}
	}

	fun loadAllSuperFurnaces() {
		if (superfurnacesData.contains("superfurnaces")) {
			val list = superfurnacesData.get("superfurnaces", null) as List<Map<String, Any?>>

			for (entry in list) {
				val location = entry["location"] as Location
				val items = entry["items"] as List<ItemStack>

				superfurnaces[location] = SuperFurnace(this, location).apply {
					for ((index, item) in items.withIndex()) {
						inventory.setItem(
							0 + index,
							item
						)
					}
				}
			}
		}
	}

	fun loadAllTrashCans() {
		if (trashcansData.contains("trashcans")) {
			val list = trashcansData.get("trashcans", null) as List<Map<String, Any?>>

			for (entry in list) {
				val location = entry["location"] as Location

				trashcans[location] = TrashCan(this, location)
			}
		}
	}

	fun saveAllMicrowaves() = saveAllLocations(
		microwavesData,
		"microwaves",
		microwaves,
		{ entry, map ->
			val items = arrayOf(
				entry.inventory.getItem(3),
				entry.inventory.getItem(4),
				entry.inventory.getItem(5)
			)

			map["items"] = items
		},
		microwavesDataFile
	)

	fun saveAllSuperFurnaces() = saveAllLocations(
		superfurnacesData,
		"superfurnaces",
		superfurnaces,
		{ entry, map ->
			val items = arrayOf(
				entry.inventory.getItem(0),
				entry.inventory.getItem(1),
				entry.inventory.getItem(2),
				entry.inventory.getItem(3),
				entry.inventory.getItem(4),
				entry.inventory.getItem(5)
			)

			map["items"] = items
		},
		superfurnacesDataFile
	)

	fun saveAllTrashCans() = saveAllLocations(
		trashcansData,
		"trashcans",
		trashcans,
		{ k, v -> },
		trashcansDataFile
	)

	private fun <T> saveAllLocations(
		configuration: YamlConfiguration,
		key: String,
		locations: Map<Location, T>,
		transformer: (T, MutableMap<Any, Any>) -> (Unit),
		file: File
	) {
		if (locations.isEmpty()) {
			logger.warning("No $key is present! Bug? We are going to ignore this save request...")
			return
		}

		logger.info("Saving ${locations.size} $key to the file...")
		configuration.set(key, null)

		val list = mutableListOf<Map<Any, Any>>()

		locations.forEach {
			val map = mutableMapOf<Any, Any>(
				"location" to it.key
			)

			transformer.invoke(it.value, map)

			list.add(map)
		}

		configuration.set(key, list)
		configuration.save(file)
	}

	private fun loadAllCustomBlocks() {
		customBlocksFolder.listFiles().filter { it.extension == "yml" }.forEach {
			val yamlConfiguration = YamlConfiguration.loadConfiguration(it)
			val list = yamlConfiguration.getStringList("blocks")
			val world = Bukkit.getWorld(it.nameWithoutExtension)
			if (world == null) {
				logger.warning { "Can't find world ${it.nameWithoutExtension}!" }
				return@forEach
			}

			val set = getCustomBlocksInWorld(it.nameWithoutExtension)

			list.forEach {
				val split = it.split(";")
				set.add(
					BlockPosition(
						split[0].toInt(),
						split[1].toInt(),
						split[2].toInt()
					)
				)
			}

			logger.info { "Loaded ${set.size} custom blocks in ${world.name}!" }
		}
	}

	private fun saveAllCustomBlocks() {
		customBlocksInWorlds.forEach {
			val yamlConfiguration = YamlConfiguration()

			val list = mutableListOf<String>()

			for (position in it.value) {
				list += "${position.x};${position.y};${position.z}"
			}

			yamlConfiguration.set("blocks", list)
			yamlConfiguration.save(File(customBlocksFolder, it.key + ".yml"))
		}
	}

	fun addRecipe(name: String, item: ItemStack, shape: List<String>, ingredients: (ShapedRecipe) -> (Unit)) {
		// create a NamespacedKey for your recipe
		val key = NamespacedKey(this, name)

		// Create our custom recipe variable
		val recipe = ShapedRecipe(key, item)

		// Here we will set the places. E and S can represent anything, and the letters can be anything. Beware; this is case sensitive.
		recipe.shape(*shape.toTypedArray())

		// Set what the letters represent.
		// E = Emerald, S = Stick
		ingredients.invoke(recipe)

		addRecipe(key, recipe)
	}

	fun addRecipe(key: NamespacedKey, recipe: Recipe) {
		recipes += key
		Bukkit.addRecipe(recipe)
	}

	fun writeDataFile(file: File) {
		if (!file.exists())
			file.writeText("")
	}
}
