package net.perfectdreams.dreamblockvipitems

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.lore
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class DreamBlockVIPItems : KotlinPlugin(), Listener {
	val ITEM_OWNER_KEY = SparklyNamespacedKey("item_owner")

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
	fun onInteract(e: PrepareAnvilEvent) {
		// If the item is in the second slots, players can remove the lore
		// So we are going to block anvil events with VIP items in the second slot
		val secondItem = e.inventory.secondItem

		if (secondItem != null && isAVIPOnlyItem(secondItem))
			e.result = ItemStack(Material.AIR)
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
		val owner = item.itemMeta?.persistentDataContainer?.get(ITEM_OWNER_KEY, PersistentDataType.STRING)

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
		itemStack
			.lore(itemStack.lore!! + "§7" + "§7Item de §a${player.name}")
			.meta<ItemMeta> {
				persistentDataContainer.set(ITEM_OWNER_KEY, PersistentDataType.STRING, player.uniqueId.toString())
			}
	}

	fun isAVIPOnlyItem(itemStack: ItemStack): Boolean {
		val requiredPermission = when {
			itemStack.lore?.any { it.contains("§7Apenas §b§lVIPs§7") } == true -> "group.vip"
			itemStack.lore?.any { it.contains("§7Apenas §b§lVIPs§e§l+§7") } == true  -> "group.vip+"
			itemStack.lore?.any { it.contains("§7Apenas §b§lVIPs§e§l++§7") } == true -> "group.vip++"
			else -> null
		}

		return requiredPermission != null
	}
}