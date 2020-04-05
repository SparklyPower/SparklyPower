package net.perfectdreams.dreamblockvipitems

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class DreamBlockVIPItems : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(this)
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onInteract(e: PlayerInteractEvent) {
		val item = e.item ?: return

		val canUse = checkIfUserCanUseTheItem(e.player, item)

		if (!canUse) {
			e.isCancelled = true
			e.player.sendMessage("§cVocê não tem poder para usar este item! Que tal comprar VIP para poder usar ele? ;)")
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onDamage(e: EntityDamageEvent) {
		val player = e.entity
		if (player !is Player)
			return

		val helmet = player.inventory.helmet
		val chestplate = player.inventory.chestplate
		val leggings = player.inventory.leggings
		val boots = player.inventory.boots

		if (helmet != null && !checkIfUserCanUseTheItem(player, helmet)) {
			val newItem = player.inventory.helmet
			player.inventory.helmet = null
			player.sendMessage("§cVocê não tem poder para usar este item! Que tal comprar VIP para poder usar ele? ;)")

			if (newItem != null)
				player.world.dropItem(player.location, newItem)
		}

		if (chestplate != null && !checkIfUserCanUseTheItem(player, chestplate)) {
			val newItem = player.inventory.chestplate
			player.inventory.chestplate = null
			player.sendMessage("§cVocê não tem poder para usar este item! Que tal comprar VIP para poder usar ele? ;)")

			if (newItem != null)
				player.world.dropItem(player.location, newItem)
		}

		if (leggings != null && !checkIfUserCanUseTheItem(player, leggings)) {
			val newItem = player.inventory.leggings
			player.inventory.leggings = null
			player.sendMessage("§cVocê não tem poder para usar este item! Que tal comprar VIP para poder usar ele? ;)")

			if (newItem != null)
				player.world.dropItem(player.location, newItem)
		}

		if (boots != null && !checkIfUserCanUseTheItem(player, boots)) {
			val newItem = player.inventory.boots
			player.inventory.boots = null
			player.sendMessage("§cVocê não tem poder para usar este item! Que tal comprar VIP para poder usar ele? ;)")

			if (newItem != null)
				player.world.dropItem(player.location, newItem)
		}
	}

	fun checkIfUserCanUseTheItem(player: Player, item: ItemStack): Boolean {
		val owner = item.getStoredMetadata("itemOwner")

		val requiredPermission = when {
			item.lore?.any { it.contains("§7Apenas §b§lVIPs§7") } == true -> "group.vip"
			item.lore?.any { it.contains("§7Apenas §b§lVIPs§e§l+§7") } == true  -> "group.vip+"
			item.lore?.any { it.contains("§7Apenas §b§lVIPs§e§l++§7") } == true -> "group.vip++"
			else -> null
		}

		if (requiredPermission != null) {
			if (player.hasPermission(requiredPermission) && item.lore?.any { it.contains("§7Apenas §b§lVIPs§7") } == true && owner == null)
				applyItemOwnership(player, item)
			if (player.hasPermission(requiredPermission) && item.lore?.any { it.contains("§7Apenas §b§lVIPs§e§l+§7") } == true && owner == null)
				applyItemOwnership(player, item)
			if (player.hasPermission(requiredPermission) && item.lore?.any { it.contains("§7Apenas §b§lVIPs§e§l++§7") } == true && owner == null)
				applyItemOwnership(player, item)
		}

		return !(requiredPermission != null && !player.hasPermission(requiredPermission) && owner != player.uniqueId.toString())
	}

	fun applyItemOwnership(player: Player, itemStack: ItemStack) {
		val index = player.inventory.indexOf(itemStack)
		player.inventory.setItem(
			index,
			itemStack
				.lore(itemStack.lore!! + "§7" + "§7Item de §a${player.name}")
				.storeMetadata("itemOwner", player.uniqueId.toString())
		)
	}
}