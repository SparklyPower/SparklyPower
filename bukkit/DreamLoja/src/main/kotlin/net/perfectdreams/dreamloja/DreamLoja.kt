package net.perfectdreams.dreamloja

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.displayNameWithoutDecorations
import net.perfectdreams.dreamcore.utils.adventure.lore
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamloja.commands.*
import net.perfectdreams.dreamloja.commands.declarations.LojaCommand
import net.perfectdreams.dreamloja.dao.UserShopVote
import net.perfectdreams.dreamloja.listeners.SignListener
import net.perfectdreams.dreamloja.listeners.TagListener
import net.perfectdreams.dreamloja.tables.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.util.*

class DreamLoja : KotlinPlugin() {
	companion object {
		val PREFIX = textComponent {
			append("[") {
				color(NamedTextColor.DARK_GRAY)
			}

			append("Loja") {
				color(NamedTextColor.GREEN)
				decorate(TextDecoration.BOLD)
			}

            append("]") {
                color(NamedTextColor.DARK_GRAY)
            }
		}

		const val VIP_PLUS_PLUS_MAX_SLOTS = 7
		const val VIP_PLUS_MAX_SLOTS = 5
		const val VIP_MAX_SLOTS = 3
		const val MEMBER_MAX_SLOTS = 1
		const val MAX_SLOT_UPGRADE_SLOTS = 9 // 54 - VIP_PLUS_PLUS_MAX_SLOTS

		val INVENTORY_POSITIONS_MAPS = mapOf(
			2 to "___X_X___",
			3 to "__X_X_X__",
			4 to "_X_X_X_X_",
			5 to "X_X_X_X_X",
			6 to "XXX___XXX",
			7 to "XXX_X_XXX",
			8 to "XXXXXXXX_",
			9 to "XXXXXXXXX",
		)
	}

	var dreamMenu: Inventory? = null
	var lastUpdate = 0L

	override fun softEnable() {
		super.softEnable()

		val hasMigratedFile = File(dataFolder, "has_migrated_items_to_new_item_serialization_format")

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				Shops,
				UserShopVotes,
				VoteSigns,
				ShopWarpUpgrades
			)

			// Convert mochilas to new ItemStack data
			if (!hasMigratedFile.exists()) {
				this@DreamLoja.logger.info("Migrating fancy icons...")
				transaction(Databases.databaseNetwork) {
					Shops.select { Shops.iconItemStack.isNotNull() }.forEach {
						val deprecatedItem = it[Shops.iconItemStack]!!.fromBase64Item()

						Shops.update({ Shops.id eq it[Shops.id] }) {
							it[Shops.iconItemStack] = ItemUtils.serializeItemToBase64(deprecatedItem)
						}
					}
				}
				hasMigratedFile.createNewFile()
			}
		}

		registerCommand(LojaCommand(this))

		registerEvents(SignListener(this))
		registerEvents(TagListener())

		scheduler().schedule(this, SynchronizationContext.ASYNC) {
			while (true) {
				waitFor(20 * (60 * 5))

				val validVotes = transaction(Databases.databaseNetwork) {
					UserShopVote.find {
						UserShopVotes.receivedAt greaterEq (System.currentTimeMillis() - 3_600_000) // Uma hora
					}.distinctBy { it.receivedBy }
				}

				if (validVotes.isEmpty())
					return@schedule

				val randomShop = validVotes.random()

				val fancyName = Bukkit.getPlayer(randomShop.receivedBy)?.displayName ?: Bukkit.getOfflinePlayer(randomShop.receivedBy)?.name ?: "???"
				val rawName = Bukkit.getOfflinePlayer(randomShop.receivedBy)?.name ?: "???"

				val str = "§6➠ §eVisite a loja d" + MeninaAPI.getArtigo(randomShop.receivedBy) + " §b$fancyName§e! §6/loja " + rawName
				switchContext(SynchronizationContext.SYNC)
				Bukkit.broadcastMessage(str)
				switchContext(SynchronizationContext.ASYNC)
			}
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	fun parseLojaName(name: String?): String {
		if (name == null)
			return "loja"

		val split = name.split(" ")
			.first()
			.lowercase()

		return split.ifBlank { "loja" }
	}

	fun parseLojaNameOrNull(name: String?): String? {
		if (name == null)
			return null

		val split = name.split(" ")
			.first()
			.lowercase()

		return split.ifEmpty { null }
	}

	fun openMenu(player: Player) {
		val diff = System.currentTimeMillis() - lastUpdate

		val currentDreamMenu = dreamMenu

		if (diff >= 900_000 || currentDreamMenu == null) {
			scheduler().schedule(this, SynchronizationContext.ASYNC) {
				val menu = generateStoreMenu()
				val newDreamMenu = menu.createInventory()
				switchContext(SynchronizationContext.SYNC)
				lastUpdate = System.currentTimeMillis()
				player.openInventory(newDreamMenu)
				dreamMenu = newDreamMenu
			}
			return
		}

		player.openInventory(currentDreamMenu)
	}

	fun generateStoreMenu(): DreamMenu {
		DreamUtils.assertAsyncThread(true)

		return createMenu(54, "§a§lComerciantes §8- §6§lLojas") {
			for (i in 1..4) {
				slot(0, i) {
					item = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
						.rename("§f")
				}
				slot(8, i) {
					item = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
						.rename("§f")
				}
			}

			for (i in 0..8) {
				if (i == 4)
					continue

				slot(i, 5) {
					item = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
						.rename("§f")
				}
			}

			for (i in 0..3) {
				val material = when (i) {
					2 -> Material.WHITE_STAINED_GLASS_PANE
					1 -> Material.CYAN_STAINED_GLASS_PANE
					else -> Material.BLUE_STAINED_GLASS_PANE
				}

				slot(i, 0) {
					item = ItemStack(material)
						.rename("§f")
				}

				slot(i + 5, 0) {
					item = ItemStack(material)
						.rename("§f")
				}
			}

			slot(4, 0) {
				item = ItemStack(Material.EMERALD)
					.meta<ItemMeta> {
						setCustomModelData(1)

						displayNameWithoutDecorations("Loja Oficial do SparklyPower") {
							color(NamedTextColor.GREEN)
							decorate(TextDecoration.BOLD)
						}
						lore {
							textWithoutDecorations("A loja oficial do SparklyPower!") {
								color(NamedTextColor.GRAY)
							}
						}
					}

				onClick { clicker ->
					clicker as Player
					clicker.closeInventory()
					clicker.performCommand("warp loja")
				}
			}

			slot(4, 5) {
				item = ItemStack(Material.NETHER_STAR)
					.meta<ItemMeta> {
						setCustomModelData(1)

						displayNameWithoutDecorations("Loja de Pesadelos do SparklyPower") {
							color(NamedTextColor.RED)
							decorate(TextDecoration.BOLD)
						}
						lore {
							textWithoutDecorations("O lugar de VIPs, Sonecas, Blocos de Proteção e muito mais!") {
								color(NamedTextColor.GRAY)
							}
						}
					}

				onClick { clicker ->
					clicker as Player
					clicker.closeInventory()
					clicker.performCommand("lojacash")
				}
			}

			val receivedByCount = UserShopVotes.receivedBy.count()

			run {
				val bestShops = transaction(Databases.databaseNetwork) {
					UserShopVotes.slice(UserShopVotes.receivedBy, receivedByCount)
						.selectAll()
						.groupBy(UserShopVotes.receivedBy)
						.orderBy(receivedByCount, SortOrder.DESC)
						.map {
							it[UserShopVotes.receivedBy] to it[receivedByCount]
						}
				}

				for (x in 1..7) {
					val bestShop = bestShops.getOrNull(x - 1)
					if (bestShop != null) {
						val (ownerUniqueId, voteCount) = bestShop

						val offlinePlayer = Bukkit.getOfflinePlayer(ownerUniqueId)

						slot(x, 1) {
							val _item = ItemStack(Material.PLAYER_HEAD)
								.rename("§a§lLoja de §b${offlinePlayer.name}")
								.storeItemLore(
									"Uma das melhores lojas de todos os tempos!",
									"Todos os Tempos",
									x,
									ownerUniqueId
								)

							val meta = _item.itemMeta as SkullMeta
							meta.owningPlayer = offlinePlayer
							_item.itemMeta = meta

							item = _item

							onClick { clicker ->
								clicker as Player
								clicker.closeInventory()
								clicker.performCommand("loja ${offlinePlayer.name}")
							}
						}
					}
				}
			}

			fun queryAndAppendTopShops(
				y: Int,
				type: String,
				shortType: String,
				time: Long
			) {
				val bestShops = transaction(Databases.databaseNetwork) {
					UserShopVotes.slice(UserShopVotes.receivedBy, receivedByCount)
						.select { UserShopVotes.receivedAt greaterEq (System.currentTimeMillis() - time) } // 30 dias
						.groupBy(UserShopVotes.receivedBy)
						.orderBy(receivedByCount, SortOrder.DESC)
						.map {
							it[UserShopVotes.receivedBy] to it[receivedByCount]
						}
				}

				for (x in 1..7) {
					val bestShop = bestShops.getOrNull(x - 1)
					if (bestShop != null) {
						val (ownerUniqueId, voteCount) = bestShop

						val offlinePlayer = Bukkit.getOfflinePlayer(ownerUniqueId)

						slot(x, y) {
							val _item = ItemStack(Material.PLAYER_HEAD)
								.rename("§a§lLoja de §b${offlinePlayer.name}")
								.storeItemLore(
									type,
									shortType,
									x,
									ownerUniqueId
								)

							val meta = _item.itemMeta as SkullMeta
							meta.owningPlayer = offlinePlayer
							_item.itemMeta = meta

							item = _item

							onClick { clicker ->
								clicker as Player
								clicker.closeInventory()
								clicker.performCommand("loja ${offlinePlayer.name}")
							}
						}
					}
				}
			}

			queryAndAppendTopShops(2, "Uma das melhores lojas nos últimos 30 dias!", "Últimos 30 dias", 2_592_000_000)
			queryAndAppendTopShops(3, "Uma das melhores lojas nos últimos 7 dias!", "Últimos 7 dias", 604_800_000)
			queryAndAppendTopShops(4, "Uma das melhores lojas na última hora!", "Última hora", 3_600_000)
		}
	}

	fun ItemStack.storeItemLore(type: String, shortType: String, x: Int, ownerUniqueId: UUID): ItemStack {
		val offlinePlayer = Bukkit.getOfflinePlayer(ownerUniqueId)
		val lastPlayed = if (Bukkit.getPlayer(ownerUniqueId) != null) {
			System.currentTimeMillis()
		} else {
			offlinePlayer.lastPlayed
		}

		val instant = Instant.ofEpochMilli(lastPlayed).atZone(ZoneId.systemDefault())

		val dayOfMonth = instant.dayOfMonth.toString().padStart(2, '0')
		val month = instant.monthValue.toString().padStart(2, '0')
		val year = instant.year.toString()

		return this.lore(
			"§6Loja §e#${x}§6 §7($shortType)§6 no SparklyPower!",
			"§d§o$type",
			"§7",
			"§2Votos: §a${transaction { UserShopVotes.select { UserShopVotes.receivedBy eq ownerUniqueId }.count() }}",
			"§2Última vez visto no SparklyPower: §a$dayOfMonth/$month/$year"
		)
	}
}