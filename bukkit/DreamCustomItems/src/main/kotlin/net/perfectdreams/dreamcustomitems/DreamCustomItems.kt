package net.perfectdreams.dreamcustomitems

import com.comphenix.protocol.ProtocolLibrary
import com.google.common.collect.Sets
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcustomitems.blocks.BlockPacketAdapter
import net.perfectdreams.dreamcustomitems.commands.CustomItemRecipeCommand
import net.perfectdreams.dreamcustomitems.commands.CustomItemsGiveExecutor
import net.perfectdreams.dreamcustomitems.commands.CustomItemsMetaExecutor
import net.perfectdreams.dreamcustomitems.commands.declarations.DreamCustomItemsCommand
import net.perfectdreams.dreamcustomitems.items.Microwave
import net.perfectdreams.dreamcustomitems.items.SuperFurnace
import net.perfectdreams.dreamcustomitems.items.TrashCan
import net.perfectdreams.dreamcustomitems.listeners.*
import net.perfectdreams.dreamcustomitems.utils.BlockPosition
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreamcustomitems.utils.repairMagnetKey
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DreamCustomItems : KotlinPlugin(), Listener {
	companion object {
		// The number of threads to use for block processing
		private const val WORKER_THREADS = 4
	}

	val mcMMO = getPlugin(com.gmail.nossr50.mcMMO::class.java)
	val dropsBlacklist = getPlugin(DreamMini::class.java).dropsBlacklist

	val microwaves = mutableMapOf<Location, Microwave>()
    val superfurnaces = mutableMapOf<Location, SuperFurnace>()

	// Custom Blocks in Worlds
	val customBlocksInWorlds = ConcurrentHashMap<String, MutableSet<BlockPosition>>()

	val customBlocksFolder = File(dataFolder, "custom_blocks")
	fun getCustomBlocksInWorld(worldName: String) = customBlocksInWorlds.getOrPut(worldName) { Sets.newConcurrentHashSet() }

	override fun softEnable() {
		super.softEnable()

		dataFolder.mkdirs()
		customBlocksFolder.mkdirs()

		loadAllCustomBlocks()

		schedule {
			while (true) {
				waitFor(20 * (15 * 60)) // every 15m

				microwaves.forEach { (location, microwave) ->
					var isEmpty = true

					for (i in 3..5) {
						if (microwave.inventory.getItem(i) !== null)
							isEmpty = false
					}

					if (isEmpty)
						microwaves.remove(location)
				}

				superfurnaces.forEach { (location, superfurnace) ->
					var isEmpty = true

					for (i in listOf(0, 1, 2, 3, 4, 5, 18, 19, 20, 21, 22, 23, 27,28, 29, 30, 31, 32)) {
						if (superfurnace.inventory.getItem(i) !== null)
							isEmpty = false
					}

					if (isEmpty)
						microwaves.remove(location)
				}

				saveAllCustomBlocks()
			}
		}

		registerEvents(BlockListener(this))
		registerEvents(CustomHeadsListener(this))
		registerEvents(BlockCraftListener(this))
		registerEvents(RubyDropListener(this))
		registerEvents(CustomBlocksListener(this))
		registerEvents(EstalinhoListener(this))
		registerEvents(MagnetListener(this))

		// https://gist.github.com/aadnk/8119275
		val protocolManager = ProtocolLibrary.getProtocolManager()
		protocolManager
			.asynchronousManager
			.registerAsyncHandler(BlockPacketAdapter(this))
			.start(WORKER_THREADS)

		registerCommand(CustomItemRecipeCommand)
		registerCommand(
			DreamCustomItemsCommand,
			CustomItemsGiveExecutor(),
			CustomItemsMetaExecutor()
		)

		addRecipe(
			repairMagnetKey,
			ShapelessRecipe(
				repairMagnetKey, Material.STONE_HOE.toItemStack()
			).addIngredient(Material.STONE_HOE)
				.addIngredient(Material.AMETHYST_SHARD)
				.addIngredient(Material.COPPER_INGOT)
		)

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

		addRecipe(
			"magnet",
			CustomItems.MAGNET,
			listOf(
				"BSB",
				"R R",
				"I I"
			)
		) {
			it.setIngredient('B', Material.IRON_BLOCK)
			it.setIngredient('S', Material.STONE)
			it.setIngredient('R', Material.PRISMARINE_SHARD)
			it.setIngredient('I', Material.IRON_INGOT)
		}

		addRecipe(
			"magnet_2",
			CustomItems.MAGNET_2,
			listOf(
				"CEC",
				"R R",
				"R R"
			)
		) {
			it.setIngredient('C', Material.CRYING_OBSIDIAN)
			it.setIngredient('E', Material.ENDER_EYE)
			it.setIngredient('R', Material.PRISMARINE_SHARD)
		}
	}

	override fun softDisable() {
		super.softDisable()

		saveAllCustomBlocks()
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

	fun writeDataFile(file: File) {
		if (!file.exists())
			file.writeText("")
	}
}
