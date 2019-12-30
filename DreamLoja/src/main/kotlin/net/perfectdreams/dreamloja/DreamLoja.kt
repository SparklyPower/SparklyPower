package net.perfectdreams.dreamloja

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamloja.commands.DelLojaCommand
import net.perfectdreams.dreamloja.commands.LojaCommand
import net.perfectdreams.dreamloja.commands.SetLojaCommand
import net.perfectdreams.dreamloja.dao.UserShopVote
import net.perfectdreams.dreamloja.listeners.SignListener
import net.perfectdreams.dreamloja.tables.Shops
import net.perfectdreams.dreamloja.tables.UserShopVotes
import net.perfectdreams.dreamloja.tables.VoteSigns
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.BlockingQueue

class DreamLoja : KotlinPlugin() {
	companion object {
		const val PREFIX = "§8[§a§lLoja§8]§e"
	}

	var dreamMenu: Inventory? = null
	var lastUpdate = 0L

	override fun softEnable() {
		super.softEnable()

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				Shops,
				UserShopVotes,
				VoteSigns
			)
		}

		registerCommand(LojaCommand(this))
		registerCommand(SetLojaCommand(this))
		registerCommand(DelLojaCommand(this))

		registerEvents(SignListener(this))

		scheduler().schedule(this, SynchronizationContext.SYNC) {
			val itemQueue = mutableListOf<ItemStack>()
			itemQueue.add(
				ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
					.rename("§7")
			)

			repeat(19) {
				itemQueue.add(
					ItemStack(Material.BLUE_STAINED_GLASS_PANE)
						.rename("§7")
				)
			}

			val trajectory = listOf(
				0,
				1,
				2,
				3,
				4,
				5,
				6,
				7,
				8,
				17,
				26,
				25,
				24,
				23,
				22,
				21,
				20,
				19,
				18,
				9
			)

			while (true) {
				if (dreamMenu != null) {
					val pop = itemQueue.get(0)
					itemQueue.removeAt(0)
					itemQueue.add(pop)

					for ((index, slot) in trajectory.withIndex()) {
						val itemToBeChanged = itemQueue[index]
						dreamMenu?.setItem(slot, itemToBeChanged)
					}
				}

				waitFor(4)
			}
		}

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

	fun openMenu(player: Player) {
		val diff = System.currentTimeMillis() - lastUpdate

		if (diff >= 900_000 || dreamMenu == null) {
			scheduler().schedule(this, SynchronizationContext.ASYNC) {
				val menu = generateStoreMenu()
				dreamMenu = menu.createInventory()
				switchContext(SynchronizationContext.SYNC)
				lastUpdate = System.currentTimeMillis()
				player.openInventory(dreamMenu)
			}
			return
		}

		player.openInventory(dreamMenu)
	}

	fun generateStoreMenu(): DreamMenu {
		DreamUtils.assertAsyncThread(true)

		return createMenu(54, "§a§lComerciantes §8- §6§lLojas") {
			for (i in 3..4) {
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

			for (i in 1..3) {
				val material = if (i == 2) {
					Material.WHITE_STAINED_GLASS_PANE
				} else {
					Material.CYAN_STAINED_GLASS_PANE
				}

				slot(i, 1) {
					item = ItemStack(material)
						.rename("§f")
				}

				slot(i + 4, 1) {
					item = ItemStack(material)
						.rename("§f")
				}
			}

			slot(4, 1) {
				item = ItemStack(Material.NETHER_STAR)
					.rename("§a§lLoja Oficial do SparklyPower")
					.lore(
						"§7A loja oficial do SparklyPower!"
					)

				onClick { clicker ->
					clicker as Player
					clicker.closeInventory()
					clicker.performCommand("warp loja")
				}
			}

			slot(4, 5) {
				item = ItemStack(Material.EMERALD)
					.rename("§c§lLoja de Pesadelos do SparklyPower")
					.lore(
						"§7O lugar de VIPs, Sonhos, Blocos de Proteção e muito mais!"
					)

				onClick { clicker ->
					clicker as Player
					clicker.closeInventory()
					clicker.performCommand("lojacash")
				}
			}

			run {
				val votes = transaction(Databases.databaseNetwork) {
					UserShopVote.find {
						UserShopVotes.receivedAt greaterEq (System.currentTimeMillis() - 2_592_000_000) // 30 dias
					}.toMutableList()
				}

				val map = mutableMapOf<UUID, Int>()

				for (vote in votes) {
					val voteCount = map.getOrDefault(vote.receivedBy, 0)
					map[vote.receivedBy] = voteCount + 1
				}

				val bestShops = map.entries.sortedByDescending { it.value }

				for (x in 1..7) {
					val bestShop = bestShops.getOrNull(x - 1)
					if (bestShop != null) {
						val (ownerUniqueId, voteCount) = bestShop

						val offlinePlayer = Bukkit.getOfflinePlayer(ownerUniqueId)

						if (offlinePlayer != null) {
							slot(x, 3) {
								val _item = ItemStack(Material.PLAYER_HEAD)
									.rename("§a§lLoja de §b${offlinePlayer.name}")
									.storeItemLore(
										"Uma das melhores lojas de todos os tempos!",
										"Últimos 30 dias",
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
			}

			run {
				val votes = transaction(Databases.databaseNetwork) {
					UserShopVote.find {
						UserShopVotes.receivedAt greaterEq (System.currentTimeMillis() - 3_600_000) // 1 hora
					}.toMutableList()
				}

				val map = mutableMapOf<UUID, Int>()

				for (vote in votes) {
					val voteCount = map.getOrDefault(vote.receivedBy, 0)
					map[vote.receivedBy] = voteCount + 1
				}

				val bestShops = map.entries.sortedByDescending { it.value }

				for (x in 1..7) {
					val bestShop = bestShops.getOrNull(x - 1)
					if (bestShop != null) {
						val (ownerUniqueId, voteCount) = bestShop

						val offlinePlayer = Bukkit.getOfflinePlayer(ownerUniqueId)

						if (offlinePlayer != null) {
							slot(x, 4) {
								val _item = ItemStack(Material.PLAYER_HEAD)
									.rename("§a§lLoja de §b${offlinePlayer.name}")
									.storeItemLore(
										"Uma das melhores lojas recentemente votadas!",
										"Última hora",
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
			}
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