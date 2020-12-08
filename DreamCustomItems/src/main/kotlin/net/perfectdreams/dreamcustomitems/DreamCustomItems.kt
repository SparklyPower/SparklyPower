package net.perfectdreams.dreamcustomitems

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcustomitems.commands.CustomItemsCommand
import net.perfectdreams.dreamcustomitems.listeners.BlockCraftListener
import net.perfectdreams.dreamcustomitems.listeners.BlockListener
import net.perfectdreams.dreamcustomitems.listeners.MicrowaveListener
import net.perfectdreams.dreamcustomitems.listeners.RubyDropListener
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreamcustomitems.utils.Microwave
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.io.File
import java.util.*

class DreamCustomItems : KotlinPlugin(), Listener {
	private val recipes = mutableListOf<NamespacedKey>()
	val microwaves = mutableMapOf<Location, Microwave>()

	val microwavesDataFile by lazy {
		File(dataFolder, "microwaves.yml")
	}

	val microwavesData by lazy {
		if (!microwavesDataFile.exists())
			microwavesDataFile.writeText("")

		YamlConfiguration.loadConfiguration(microwavesDataFile)
	}

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()

		loadAllMicrowaves()

		schedule {
			while (true) {
				waitFor(20 * (15 * 60)) // every 15m
				saveAllMicrowaves()
			}
		}

		registerEvents(BlockListener(this))
		registerEvents(MicrowaveListener(this))
		registerEvents(BlockCraftListener(this))
		registerEvents(RubyDropListener(this))

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
	}

	override fun softDisable() {
		super.softDisable()

		saveAllMicrowaves()

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