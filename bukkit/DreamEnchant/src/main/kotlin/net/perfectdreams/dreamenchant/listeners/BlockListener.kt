package net.perfectdreams.dreamenchant.listeners

import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamenchant.DreamEnchant
import net.perfectdreams.dreamenchant.utils.PlayerEnchantmentTable
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.block.EnchantingTable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class BlockListener(val m: DreamEnchant) : Listener {
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onCraft(e: CraftItemEvent) {
		val recipe = e.recipe
		if (recipe is Keyed) {
			val recipeKey = recipe.key.key == "super_enchanting_table"

			if (recipeKey && e.inventory.any { it.type == Material.PRISMARINE_SHARD && (!it.itemMeta.hasCustomModelData() || it.itemMeta.customModelData != 1) })
				e.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onEnchantmentTablePlace(e: BlockPlaceEvent) {
		val itemInHand = e.itemInHand
		if (!itemInHand.hasItemMeta())
			return

		val itemMeta = itemInHand.itemMeta
		if (!itemMeta.persistentDataContainer.has(PlayerEnchantmentTable.SUPER_ENCHANTMENT_TABLE_CREDITS))
			return

		val enchantingTable = e.blockPlaced.state as EnchantingTable
		enchantingTable.persistentDataContainer.set(
			PlayerEnchantmentTable.SUPER_ENCHANTMENT_TABLE_CREDITS,
			PersistentDataType.INTEGER,
			itemMeta.persistentDataContainer.get(PlayerEnchantmentTable.SUPER_ENCHANTMENT_TABLE_CREDITS, PersistentDataType.INTEGER) ?: 0
		)
		enchantingTable.update()
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onEnchantmentTableBreak(e: BlockDropItemEvent) {
		val blockState = e.blockState
		if (blockState !is EnchantingTable)
			return

		if (!blockState.persistentDataContainer.has(PlayerEnchantmentTable.SUPER_ENCHANTMENT_TABLE_CREDITS))
			return

		val enchantmentTableItem = e.items.firstOrNull { it.itemStack.type == Material.ENCHANTING_TABLE }
		// Change the first dropped enchantment table item from the list
		enchantmentTableItem?.itemStack = m.createSpecialEnchantmentTableItemStack(
			blockState.persistentDataContainer.get(PlayerEnchantmentTable.SUPER_ENCHANTMENT_TABLE_CREDITS, PersistentDataType.INTEGER) ?: 0
		)
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onInteract(e: PlayerInteractEvent) {
		if (e.action != Action.RIGHT_CLICK_BLOCK)
			return

		val clickedBlock = e.clickedBlock

		if (clickedBlock?.type != Material.ENCHANTING_TABLE)
			return

		// Spawn Enchantment Table
		if (clickedBlock.location.isWithinRegion("spawn_enchantment")) {
			e.isCancelled = true

			m.spawnEnchantmentTable.openEnchantmentInventoryOrCreditsScreen(
				e.player,
				clickedBlock,
				0
			)
		} else {
			val enchantingTable = clickedBlock.state as EnchantingTable
			if (enchantingTable.persistentDataContainer.has(PlayerEnchantmentTable.SUPER_ENCHANTMENT_TABLE_CREDITS)) {
				e.isCancelled = true

				// Super Enchanting Table
				m.playerEnchantmentTable.openEnchantmentInventoryOrCreditsScreen(
					e.player,
					clickedBlock,
					0
				)
			}
		}
	}
}