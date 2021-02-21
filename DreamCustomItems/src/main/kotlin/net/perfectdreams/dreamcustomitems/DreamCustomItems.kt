package net.perfectdreams.dreamcustomitems

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcustomitems.commands.CustomItemRecipeCommand
import net.perfectdreams.dreamcustomitems.commands.CustomItemsCommand
import net.perfectdreams.dreamcustomitems.items.Microwave
import net.perfectdreams.dreamcustomitems.items.SuperFurnace
import net.perfectdreams.dreamcustomitems.items.TrashCan
import net.perfectdreams.dreamcustomitems.listeners.BlockCraftListener
import net.perfectdreams.dreamcustomitems.listeners.BlockListener
import net.perfectdreams.dreamcustomitems.listeners.CustomHeadsListener
import net.perfectdreams.dreamcustomitems.listeners.RubyDropListener
import net.perfectdreams.dreamcustomitems.utils.CustomItems
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

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()

		loadAllMicrowaves()
		loadAllSuperFurnaces()
		loadAllTrashCans()

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
						"I I",
						"III"
				)
		) {
			it.setIngredient('I', Material.IRON_INGOT)
		}
	}

	override fun softDisable() {
		super.softDisable()

		saveAllMicrowaves()
		saveAllSuperFurnaces()
		saveAllTrashCans()

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

	fun saveAllTrashCans() {
		trashcansData.set("trashcans", null)

		val list = mutableListOf<Map<Any, Any>>()

		trashcans.map {

			list.add(
					mapOf(
							"location" to it.key
					)
			)
		}

		trashcansData.set("trashcans", list)
		trashcansData.save(trashcansDataFile)
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
