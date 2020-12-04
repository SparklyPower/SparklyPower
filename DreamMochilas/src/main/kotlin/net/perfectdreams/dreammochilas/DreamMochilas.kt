package net.perfectdreams.dreammochilas

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.listeners.InventoryListener
import net.perfectdreams.dreammochilas.tables.Mochilas
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Listener
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

		val config = YamlConfiguration.loadConfiguration(File(dataFolder, "funny.yml"))

		FunnyIds.names.addAll(config.getStringList("Names"))
		FunnyIds.adjectives.addAll(config.getStringList("Adjectives"))

		registerCommand(
				object: SparklyCommand(arrayOf("mochila"), "sparklymochilas.give") {
					@Subcommand(["get"])
					fun mochila(sender: Player, damageValue: String = "1") {
						val item = createMochila(damageValue.toInt())

						sender.inventory.addItem(item)

						sender.sendMessage("Prontinho patrão, usando meta value $damageValue")
					}

					@Subcommand(["player"])
					fun getPlayerMochilas(sender: Player, playerName: String, skip: String? = null) {
						scheduler().schedule(INSTANCE) {
							switchContext(SynchronizationContext.ASYNC)
							val uniqueId = DreamUtils.retrieveUserUniqueId(playerName)
							switchContext(SynchronizationContext.SYNC)

							sender.sendMessage("§aCriando inventário com mochilas de $uniqueId")

							val mochilas = transaction(Databases.databaseNetwork) {
								Mochila.find {
									Mochilas.owner eq uniqueId
								}.toMutableList()
							}

							val inventory = Bukkit.createInventory(null, 54)
							mochilas.forEach {
								inventory.addItem(
									it.createItem()
								)
							}

							sender.openInventory(inventory)

							sender.sendMessage("§7É possível pular entradas usando §6/mochila player $playerName QuantidadeDeMochilasParaPular")
						}
					}
				}
		)
	}
}