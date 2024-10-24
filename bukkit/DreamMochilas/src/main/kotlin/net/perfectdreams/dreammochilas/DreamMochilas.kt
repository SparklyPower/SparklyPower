package net.perfectdreams.dreammochilas

import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomCraftingRecipe
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import net.perfectdreams.dreammochilas.commands.declarations.MochilaCommand
import net.perfectdreams.dreammochilas.listeners.ChestShopListener
import net.perfectdreams.dreammochilas.listeners.InventoryListener
import net.perfectdreams.dreammochilas.listeners.UpgradeSizeSignListener
import net.perfectdreams.dreammochilas.tables.Mochilas
import net.perfectdreams.dreammochilas.utils.MochilaData
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.File

class DreamMochilas : KotlinPlugin(), Listener {
	companion object {
		lateinit var INSTANCE: DreamMochilas

		fun createMochila(mochilaData: MochilaData): ItemStack {
			val item = ItemStack(Material.PAPER)
				.rename("§r${if (mochilaData.displayName != null) mochilaData.displayName else "Mochila"}")
				.meta<ItemMeta> {
					setCustomModelData(mochilaData.customModelData)

					persistentDataContainer.set(
						MochilaUtils.IS_MOCHILA_KEY,
						PersistentDataType.BYTE,
						1
					)
				}

			return item
		}

		fun createMochilaOldSystem(damageValue: Int): ItemStack {
			val item = ItemStack(Material.CARROT_ON_A_STICK)
				.rename("§rMochila")
				.meta<ItemMeta> {
					persistentDataContainer.set(
						MochilaUtils.IS_MOCHILA_KEY,
						PersistentDataType.BYTE,
						1
					)
				}

			val meta = item.itemMeta
			meta as Damageable
			meta.damage = damageValue
			item.itemMeta = meta

			val meta2 = item.itemMeta
			meta2.isUnbreakable = true
			item.itemMeta = meta2

			return item
		}

		private val mochilaWindowCharacters = setOf(
			'\uE256',
			'\uE257',
			'\uE258',
			'\uE255'
		)
	}

	override fun softEnable() {
		super.softEnable()

		val hasMigratedFile = File(dataFolder, "has_migrated_items_to_new_item_serialization_format")

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				Mochilas
			)

			// Convert mochilas to new ItemStack data
			if (!hasMigratedFile.exists()) {
				this@DreamMochilas.logger.info("Migrating Mochilas...")
				transaction(Databases.databaseNetwork) {
					Mochilas.selectAll().forEach {
						try {
							val deprecatedInventory = it[Mochilas.content].fromBase64Inventory()

							val map = mutableMapOf<Int, String?>()

							deprecatedInventory.contents.forEachIndexed { index, itemStack ->
								map[index] = itemStack?.let { ItemUtils.serializeItemToBase64(it) }
							}

							Mochilas.update({ Mochilas.id eq it[Mochilas.id] }) {
								it[Mochilas.content] = Json.encodeToString(map)
							}
						} catch (e: Exception) {
							this@DreamMochilas.logger.warning("Failed to migrate mochila ${it[Mochilas.id]} (size:  ${it[Mochilas.size]})")
						}
					}
				}
				hasMigratedFile.createNewFile()
			}
		}

		INSTANCE = this

		registerEvents(InventoryListener(this))
		registerEvents(ChestShopListener())
		registerEvents(UpgradeSizeSignListener(this))

		val config = YamlConfiguration.loadConfiguration(File(dataFolder, "funny.yml"))

		FunnyIds.names.addAll(config.getStringList("Names"))
		FunnyIds.adjectives.addAll(config.getStringList("Adjectives"))

		registerCommand(MochilaCommand(this))

		val bedrockIntegrations = Bukkit.getPluginManager().getPlugin("DreamBedrockIntegrations") as DreamBedrockIntegrations
		bedrockIntegrations.registerInventoryTitleTransformer(
			this,
			{
				PlainTextComponentSerializer.plainText().serialize(it)
					.any { it in mochilaWindowCharacters }
			},
			{
				// The last children *should* be the mochila's name, if it is not present, just fallback to the current title
				it.children().lastOrNull() ?: it
			}
		)

		launchAsyncThread {
			while (true) {
				// Save all backpacks
				logger.info { "Saving all mochilas to the database... (Periodic Save)" }

				for (loadedMochila in MochilaUtils.loadedMochilas.values) {
					loadedMochila.saveMochila(
						"Periodic Save",
						removeFromMemory = false
					)
				}

				delay(60_000)
			}
		}

		DreamCustomItems.registerCustomRecipe(
			CustomCraftingRecipe(
				this,
				itemRemapper = {
					when (it.type) {
						Material.WHITE_WOOL -> {
							CustomItems.RAINBOW_WOOL
						}
						Material.PAPER -> {
							createMochila(MochilaData.Brown)
						}
						else -> ItemStack(it)
					}
				},
				checkRemappedItems = false,
				recipe = addRecipe(
					"rainbow_mochila",
					createMochila(MochilaData.Rainbow),
					listOf(
						" R ",
						"RMR",
						" R "
					)
				) {
					it.setIngredient('R', Material.WHITE_WOOL)
					it.setIngredient('M', Material.PAPER)
				}
			)
		)
	}

	override fun softDisable() {
		// Save all backpacks
		for (loadedMochila in MochilaUtils.loadedMochilas.values) {
			loadedMochila.saveMochila(
				"Server is shutting down",
				bypassAssertAsyncThreadCheck = true
			)
		}
	}
}
