package net.perfectdreams.dreaminventorysnapshots

import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.ItemUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreaminventorysnapshots.commands.InventorySnapshotsCommand
import net.perfectdreams.dreaminventorysnapshots.tables.InventorySnapshots
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

class DreamInventorySnapshots : KotlinPlugin(), Listener {
	companion object {
		val SNAPSHOT_DELAY = 5.minutes
	}

	override fun softEnable() {
		super.softEnable()

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				InventorySnapshots
			)
		}

		registerCommand(InventorySnapshotsCommand(this))

		this.launchMainThread {
			while (true) {
				logger.info("Snapshotting inventories...")

				val playerSerializedInventories = mutableMapOf<Player, Map<Int, String?>>()

				val now = Instant.now()
				for (player in Bukkit.getOnlinePlayers()) {
					val map = mutableMapOf<Int, String?>()

					player.inventory.contents.forEachIndexed { index, itemStack ->
						map[index] = itemStack?.let { ItemUtils.serializeItemToBase64(it) }
					}

					playerSerializedInventories[player] = map
				}

				onAsyncThread {
					transaction(Databases.databaseNetwork) {
						InventorySnapshots.batchInsert(
							playerSerializedInventories.entries,
							shouldReturnGeneratedValues = false
						) {
							this[InventorySnapshots.playerId] = it.key.uniqueId
							this[InventorySnapshots.createdAt] = now
							this[InventorySnapshots.content] = Json.encodeToString(it.value)
						}
					}
				}

				logger.info("Successfully snapshotted ${playerSerializedInventories.size} player inventories!")

				delay(SNAPSHOT_DELAY)
			}
		}
	}

	override fun softDisable() {
		super.softDisable()
	}
}
