package net.perfectdreams.dreamlobbyfun.listeners

import com.github.salomonbrys.kotson.jsonObject
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import net.perfectdreams.dreamlobbyfun.tables.UserSettings
import net.perfectdreams.dreamlobbyfun.utils.CycleGlass
import net.perfectdreams.dreamlobbyfun.utils.ServerSelectorHolder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class InventoryListener(val m: DreamLobbyFun) : Listener {
	@EventHandler
	fun onDrop(e: PlayerDropItemEvent) {
		if (!m.unlockedPlayers.contains(e.player)) {
			e.isCancelled = true
		}
	}

	@EventHandler
	fun onInventory(e: InventoryClickEvent) {
		if (!m.unlockedPlayers.contains(e.whoClicked) || e.clickedInventory?.holder is ServerSelectorHolder)
			e.isCancelled = true

		if (e.clickedInventory?.holder is ServerSelectorHolder) { // Clicou dentro do server selector
			val info = e.currentItem?.getStoredMetadata(DreamLobbyFun.ITEM_INFO_KEY) ?: return

			val split = info.split(":")
			val arg0 = split.getOrNull(0)
			val arg1 = split.getOrNull(1)

			if (arg0 == "transferTo") {
				if (m.teleportToLoginLocationIfNotLoggedIn(e.whoClicked as Player))
					return

				e.whoClicked.closeInventory()
				// player.sendTitle("§eTransferindo...", "", 10, 60, 10)
				DreamNetwork.PERFECTDREAMS_BUNGEE.sendAsync(
						jsonObject(
								"type" to "transferPlayer",
								"player" to e.whoClicked.name,
								"bungeeServer" to arg1
						)
				)
			}
		}
	}

	@EventHandler
	fun onInteract(e: PlayerInteractEntityEvent) {
		if (e.rightClicked.type != EntityType.PLAYER)
			return

		if (!m.unlockedPlayers.contains(e.player)) {
			e.isCancelled = true
		}
	}

	@EventHandler
	fun onInteract(e: PlayerInteractAtEntityEvent) {
		if (e.rightClicked.type != EntityType.PLAYER)
			return

		if (!m.unlockedPlayers.contains(e.player)) {
			e.isCancelled = true
		}
	}

	@EventHandler
	fun onClick(e: PlayerInteractEvent) {
		val clicker = e.player

		val info = e.item?.getStoredMetadata(DreamLobbyFun.ITEM_INFO_KEY) ?: return

		e.isCancelled = true

		val split = info.split(":")
		val arg0 = split.getOrNull(0)
		val arg1 = split.getOrNull(1)

		if (arg0 == "serverSelector") {
			if (m.teleportToLoginLocationIfNotLoggedIn(e.player as Player))
				return

			val inventory = Bukkit.createInventory(ServerSelectorHolder(), 45, "§a§lEscolha um Servidor!")

			val outline = listOf(
					0,
					9,
					18,
					27,
					36,
					37,
					38,
					39,
					40,
					41,
					42,
					43,
					44,
					35,
					26,
					17,
					8,
					7,
					6,
					5,
					4,
					3,
					2,
					1
			)

			var lastCycle: CycleGlass? = null
			val slots = mutableMapOf<Int, CycleGlass>()

			for (slot in outline) {
				val glass = CycleGlass()
				if (lastCycle != null) {
					glass.damageValue = lastCycle.damageValue
				}
				glass.next()
				slots[slot] = glass
				lastCycle = glass
			}

			clicker.openInventory(inventory)

			scheduler().schedule(m) {
				while (true) {
					if (!clicker.isValid)
						return@schedule

					if (clicker.openInventory?.topInventory != inventory)
						return@schedule

					for ((slot, glass) in slots) {
						inventory.setItem(slot, ItemStack(glass.next(), 1))
					}
					waitFor(4)
				}
			}

			var blink = false

			scheduler().schedule(m) {
				while (true) {
					if (!clicker.isValid)
						return@schedule

					if (clicker.openInventory?.topInventory != inventory)
						return@schedule

					run {
						var survival = ItemStack(Material.DIAMOND_PICKAXE)
						survival.addEnchantment(Enchantment.DURABILITY, 1)
						survival.addFlag(ItemFlag.HIDE_ENCHANTS)
						survival.rename("§6✪ §a§lSparklyPower Survival §6✪")

						// if (survivalPlayers != -1) {
						survival.lore(
							"§a${if (blink) "  " else "➤"} Clique para entrar!",
							"§a§l» §r§aAtualmente com §b${DreamLobbyFun.SERVER_ONLINE_COUNT["sparklypower_survival"]} players §aconectados!",
							"§7",
							"§7O Melhor Servidor Survival do Brasil, Sem Exceções.",
							"§7",
							"§7Desde 2014 trazendo experiências inovadoras que jamais",
							"§7foram vistas antes em outros servidores.",
							"§7",
							"§7Afinal, em qual servidor você já viu jetpacks, coxinhas, fuscas, e muito mais?",
							"§7",
							"§7...e ainda por cima é o servidor oficial da Loritta!",
							"§7",
							"§7Entre agora... porque só falta você!"
						)
						// } else {
						// 	survival.lore("§c${if (blink) "  " else "✖"} Servidor offline...", "§c§l» §r§cDesculpe pela inconveniência... :(", "§7", "§7Em breve... ;)")
						// }

						survival = survival.addFlag(ItemFlag.HIDE_ATTRIBUTES)
								.storeMetadata(DreamLobbyFun.ITEM_INFO_KEY, "transferTo:sparklypower_survival")

						inventory.setItem(22, survival)
					}

					blink = !blink
					waitFor(10)
				}
			}
			return
		}

		if (arg0 == "setPlayerVisibility" && arg1 != null) {
			val bool = arg1.toBoolean()

			if (bool) {
				for (player in Bukkit.getOnlinePlayers()) {
					clicker.showPlayer(m, player)
				}

				clicker.sendMessage("§aVocê não está mais sozinho! Agora você consegue ver todos os outros jogadores!")
			} else {
				for (player in Bukkit.getOnlinePlayers()) {
					clicker.hidePlayer(m, player)
				}

				clicker.sendMessage("§aPaz e Tranquilidade... Agora todos os outros jogadores magicamente desapareceram para você!")
			}

			clicker.inventory.setItem(0,
					if (bool) {
						ItemStack(Material.LIME_DYE, 1, 10).rename("§a§lPlayers estão visíveis")
								.lore("§7Cansado de aturar as outras pessoas?", "§7", "§7Então clique para deixar todas", "§7as outras pessoas invisíveis!")
								.storeMetadata(DreamLobbyFun.ITEM_INFO_KEY, "setPlayerVisibility:false")
					} else {
						ItemStack(Material.GRAY_DYE, 1, 8).rename("§c§lPlayers estão invisíveis")
								.lore("§7Está se sentindo sozinho?", "§7", "§7Então clique para deixar todas", "§7as outras pessoas visíveis!")
								.storeMetadata(DreamLobbyFun.ITEM_INFO_KEY, "setPlayerVisibility:true")
					}
			)

			scheduler().schedule(m, SynchronizationContext.ASYNC) {
				transaction(Databases.databaseNetwork) {
					UserSettings.update({ UserSettings.id eq clicker.uniqueId }) {
						it[playerVisibility] = bool
					}
				}
			}
		}
	}
}
