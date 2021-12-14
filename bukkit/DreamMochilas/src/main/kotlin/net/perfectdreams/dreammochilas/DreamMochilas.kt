package net.perfectdreams.dreammochilas

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammochilas.commands.*
import net.perfectdreams.dreammochilas.commands.declarations.MochilaCommand
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.listeners.ChestShopListener
import net.perfectdreams.dreammochilas.listeners.InventoryListener
import net.perfectdreams.dreammochilas.listeners.UpgradeSizeSignListener
import net.perfectdreams.dreammochilas.tables.Mochilas
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

class DreamMochilas : KotlinPlugin(), Listener {
	companion object {
		lateinit var INSTANCE: DreamMochilas

		fun createMochila(damageValue: Int): ItemStack {
			val item = ItemStack(Material.CARROT_ON_A_STICK)
				.rename("§rMochila")
				.storeMetadata("isMochila", "true")

			val meta = item.itemMeta
			meta as Damageable
			meta.damage = damageValue
			item.itemMeta = meta

			val meta2 = item.itemMeta
			meta2.isUnbreakable = true
			item.itemMeta = meta2

			return item
		}
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
			GetMochilaIdExecutor(),
			GetPlayerMochilasExecutor(),
			MochilasMemoryExecutor(this),
			FakeInteractAndOpenExecutor(this),
			FakeInteractAutoClickExecutor(this)
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
