package net.perfectdreams.dreammapwatermarker

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemFlag
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class DreamMapWatermarker : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(this)

		registerCommand(
			command("DreamWatermarkMap", listOf("watermarkmap")) {
				permission = "dreamwatermarkmap.watermark"

				executes {
					val playerName = args.getOrNull(0) ?: run {
						player.sendMessage("§cVocê precisa colocar o nome do player!")
						return@executes
					}

					schedule(SynchronizationContext.ASYNC) {
						val uniqueId = DreamUtils.retrieveUserUniqueId(playerName)

						switchContext(SynchronizationContext.SYNC)

						val item = player.inventory.itemInMainHand

						player.inventory.setItemInMainHand(
							item.lore(
								"§7Diretamente de §dXerox da Pantufa§7...",
								"§7(temos os melhores preços da região!)",
								"§7§oUm incrível mapa para você!",
								"§7",
								"§7Mapa feito para §a${playerName} §e(◠‿◠✿)"
							).apply {
								this.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1)
								this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
							}.storeMetadata("customMapOwner", uniqueId.toString())
						)
					}
				}
			}
		)
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		val hasCustomMap = event.inventory.matrix.filterNotNull().any {
			it.getStoredMetadata("customMapOwner") != null || it.lore?.lastOrNull() == "§a§lObrigado por votar! ^-^" || it.itemMeta?.displayName?.endsWith("Players Online!") == true
		}

		if (hasCustomMap)
			event.isCancelled = true
	}

	@EventHandler
	fun onCartography(event: InventoryClickEvent) {
		// We could use "clickedInventory" but that does disallow dragging from the bottom to the top
		val clickedInventory = event.whoClicked.openInventory
		val currentItem = event.currentItem ?: return

		if (clickedInventory.type != InventoryType.CARTOGRAPHY) // el gambiarra
			return

		if (currentItem.getStoredMetadata("customMapOwner") != null || currentItem.lore?.lastOrNull() == "§a§lObrigado por votar! ^-^" || currentItem.itemMeta?.displayName?.endsWith("Players Online!") == true) {
			event.isCancelled = true
		}

		// Bukkit.broadcastMessage("Moveu item ${event.destination}")
	}
}