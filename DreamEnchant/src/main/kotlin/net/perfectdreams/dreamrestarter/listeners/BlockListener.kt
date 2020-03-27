package net.perfectdreams.dreamenchant.listeners

import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamenchant.DreamEnchant
import net.perfectdreams.dreamenchant.utils.EnchantHolder
import net.perfectdreams.dreamenchant.utils.EnchantUtils
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent

class BlockListener(val m: DreamEnchant) : Listener {
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onInteract(e: PlayerInteractEvent) {
		if (e.action != Action.RIGHT_CLICK_BLOCK)
			return

		val clickedBlock = e.clickedBlock

		if (clickedBlock?.type != Material.ENCHANTING_TABLE)
			return

		if (!clickedBlock.location.isWithinRegion("spawn_enchantment"))
			return

		e.isCancelled = true
		EnchantUtils.openEnchantmentInventory(e.player, 0)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onClick(e: InventoryClickEvent) {
		val holder = e.inventory.holder

		if (holder !is EnchantHolder)
			return

		val item = e.currentItem
		if (item == null || item.type == Material.AIR)
			return

		e.isCancelled = true

		val player = e.whoClicked as Player

		val inventoryAction = item.getStoredMetadata("inventoryAction")

		if (inventoryAction == "close") {
			player.closeInventory()
		} else if (inventoryAction == "old") {
			player.openEnchanting(Location(Bukkit.getWorld("world"), 494.0,63.0, 176.0), false)
		} else if (inventoryAction == "go") {
			val newPage = item.getStoredMetadata("newPage")!!.toInt()

			player.closeInventory()
			EnchantUtils.openEnchantmentInventory(player, newPage)
		} else {
			val enchantWith = item.getStoredMetadata("enchantWith")
			val itemInHandHash = item.getStoredMetadata("itemInHandHash")
			val enchantmentLevel = item.getStoredMetadata("enchantmentLevel")

			if (enchantWith != null && itemInHandHash != null && enchantmentLevel != null) {
				val heldItem = player.inventory.itemInMainHand
				val newHash = heldItem.hashCode()

				player.closeInventory()

				if (itemInHandHash.toInt() != newHash) {
					player.sendMessage("§cVocê alterou o item que está na sua mão!")
					return
				}

				val enchantment = Enchantment.getByName(enchantWith)

				val hasConflict = heldItem.enchantments.any {
					it.key != enchantment && it.key.conflictsWith(enchantment)
				}

				if (hasConflict) {
					player.sendMessage("§cO encantamento que você quer aplicar tem conflito com outro encantamento que está no seu item!")
					return
				}

				val levelCost = enchantmentLevel.toInt() *  EnchantUtils.getLevelMultiplierForPlayer(player)

				if (levelCost > player.level) {
					player.sendMessage("§cVocê não possui experiência suficiente para encantar este item!")
					return
				}

				heldItem.addEnchantment(enchantment, enchantmentLevel.toInt())
				player.level = (player.level - levelCost)
				player.sendMessage("§aO seu item foi encantado com sucesso!")
				player.world.spawnParticle(Particle.VILLAGER_HAPPY, player.location, 20, 1.0, 1.0, 1.0)
				player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
			}
		}
	}
}