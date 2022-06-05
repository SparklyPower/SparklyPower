package net.perfectdreams.dreammochilas

import kotlinx.coroutines.delay
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.rename
import net.perfectdreams.dreammochilas.commands.*
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
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class DreamMochilas : KotlinPlugin(), Listener {
	companion object {
		lateinit var INSTANCE: DreamMochilas

		fun createMochila(mochilaData: MochilaData): ItemStack {
			val item = ItemStack(Material.CHEST_MINECART)
				.rename("§rMochila")
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

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				Mochilas
			)
		}

		INSTANCE = this

		registerEvents(InventoryListener(this))
		registerEvents(ChestShopListener())
		registerEvents(UpgradeSizeSignListener(this))

		val config = YamlConfiguration.loadConfiguration(File(dataFolder, "funny.yml"))

		FunnyIds.names.addAll(config.getStringList("Names"))
		FunnyIds.adjectives.addAll(config.getStringList("Adjectives"))

		registerCommand(
			MochilaCommand,
			GetMochilaExecutor(),
			GetMochilaOldDamageSystemExecutor(),
			GetMochilaIdExecutor(),
			GetPlayerMochilasExecutor(),
			MochilasMemoryExecutor(this),
			FakeInteractAndOpenExecutor(this),
			FakeInteractAutoClickExecutor(this)
		)

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

		addRecipe(
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
