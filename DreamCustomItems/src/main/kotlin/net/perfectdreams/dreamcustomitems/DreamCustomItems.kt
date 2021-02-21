package net.perfectdreams.dreamcustomitems

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcustomitems.commands.CustomItemsCommand
import net.perfectdreams.dreamcustomitems.listeners.*
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreamcustomitems.utils.Microwave
import net.perfectdreams.dreamcustomitems.utils.SuperFurnace
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import java.io.File

class DreamCustomItems : KotlinPlugin(), Listener {
	private val recipes = mutableListOf<NamespacedKey>()
	val microwaves = mutableMapOf<Location, Microwave>()
	val superfurnaces = mutableMapOf<Location, SuperFurnace>()

	val microwavesDataFile by lazy {
		File(dataFolder, "microwaves.yml")
	}

	val microwavesData by lazy {
		if (!microwavesDataFile.exists())
			microwavesDataFile.writeText("")

		YamlConfiguration.loadConfiguration(microwavesDataFile)
	}

	val superfurnacesDataFile by lazy {
		File(dataFolder, "superfurnaces.yml")
	}

	val superfurnacesData by lazy {
		if (!superfurnacesDataFile.exists())
			superfurnacesDataFile.writeText("")

		YamlConfiguration.loadConfiguration(superfurnacesDataFile)
	}

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()

		loadAllMicrowaves()
		loadAllSuperFurnaces()

		schedule {
			while (true) {
				waitFor(20 * (15 * 60)) // every 15m
				saveAllMicrowaves()
				saveAllSuperFurnaces()
			}
		}

		registerEvents(BlockListener(this))
		registerEvents(CustomHeadsListener(this))
		registerEvents(BlockCraftListener(this))
		registerEvents(RubyDropListener(this))
		registerEvents(RainbowWoolListener())

		registerCommand(CustomItemsCommand)

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
	}

	override fun softDisable() {
		super.softDisable()

		saveAllMicrowaves()
		saveAllSuperFurnaces()

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

	fun saveAllMicrowaves() {
		microwavesData.set("microwaves", null)

		val list = mutableListOf<Map<Any, Any>>()

		microwaves.map {
			val items = arrayOf(
				it.value.inventory.getItem(3),
				it.value.inventory.getItem(4),
				it.value.inventory.getItem(5)
			)

			list.add(
				mapOf(
					"location" to it.key,
					"items" to items
				)
			)
		}

		microwavesData.set("microwaves", list)
		microwavesData.save(microwavesDataFile)
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

	fun saveAllSuperFurnaces() {
		superfurnacesData.set("superfurnaces", null)

		val list = mutableListOf<Map<Any, Any>>()

		superfurnaces.map {
			val items = arrayOf(
					it.value.inventory.getItem(0),
					it.value.inventory.getItem(1),
					it.value.inventory.getItem(2),
					it.value.inventory.getItem(3),
					it.value.inventory.getItem(4),
					it.value.inventory.getItem(5)
			)

			list.add(
					mapOf(
							"location" to it.key,
							"items" to items
					)
			)
		}

		superfurnacesData.set("superfurnaces", list)
		superfurnacesData.save(superfurnacesDataFile)
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
}
