package net.perfectdreams.dreamquickharvest

import com.gmail.nossr50.api.ExperienceAPI
import com.gmail.nossr50.mcMMO
import com.okkero.skedule.BukkitDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.extensions.canBreakAt
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import kotlin.experimental.and

class DreamQuickHarvest : KotlinPlugin(), Listener {
	val useRadiusHarvest = true

	override fun softEnable() {
		super.softEnable()

		registerEvents(this)
	}

	override fun softDisable() {
		super.softDisable()
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	@InternalCoroutinesApi
	fun onBreak(e: BlockBreakEvent) {
		if (e.player.isSneaking)
			return

		if (e.block.type == Material.WHEAT // Crops
			|| e.block.type == Material.NETHER_WART
			|| e.block.type == Material.CARROTS
			|| e.block.type == Material.POTATOES
			|| e.block.type == Material.BEETROOTS
			|| e.block.type == Material.PUMPKIN
			|| e.block.type == Material.MELON) {
			if (!shouldCancelCropEvent(e, e.block))
				return

			e.isCancelled = true

			GlobalScope.launch(BukkitDispatcher(this)) {
				doQuickHarvestOnCrop(e.player, e.block, e.block.type, e.player.inventory.itemInMainHand?.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) ?: 0)
			}
			return
		}

		if (e.block.type == Material.COCOA) {
			if (!shouldCancelCocoaEvent(e, e.block))
				return

			e.isCancelled = true

			GlobalScope.launch(BukkitDispatcher(this)) {
				doQuickHarvestOnCocoa(e, e.player, e.block)
			}
			return
		}

		if (e.block.type == Material.SUGAR_CANE && (e.block.getRelative(BlockFace.DOWN).type == Material.SUGAR_CANE || e.block.getRelative(BlockFace.UP).type == Material.SUGAR_CANE)) { // Apenas execute o quick harvest caso seja uma plantação de sugar cane
			e.isCancelled = true

			GlobalScope.launch(BukkitDispatcher(this)) {
				doQuickHarvestOnSugarCane(e, e.player, e.block)
			}
			return
		}
	}

	fun shouldCancelCropEvent(e: BlockBreakEvent, block: Block): Boolean {
		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return false

		val damage = block.data

		val fullyGrown = when (block.type) {
			Material.MELON -> true
			Material.PUMPKIN -> true
			Material.NETHER_WART -> damage == 3.toByte()
			Material.BEETROOTS -> damage == 3.toByte()
			else -> damage == 7.toByte()
		}

		return fullyGrown
	}

	fun shouldCancelCocoaEvent(e: BlockBreakEvent, block: Block): Boolean {
		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return false

		val stage = block.data and 12

		if (stage != 8.toByte()) // CocoaPlant.class
			return false

		return true
	}

	fun giveMcMMOHerbalismXP(player: Player, block: Block, material: Material? = null){
		if (mcMMO.getPlaceStore().isTrue(block.state))
			return

		var xpBlockPlant = 0

		if(material != null){
			xpBlockPlant = when(material){
				Material.PUMPKIN -> 20
				Material.MELON -> 20
				else -> 0
			}
		}

		val xpValue = when(block.type){
			Material.COCOA -> 30
			Material.SUGAR_CANE -> 30
			Material.NETHER_WART -> 50
			Material.BEETROOTS -> 50
			Material.POTATOES -> 50
			Material.CARROTS -> 50
			Material.WHEAT -> 50
			else -> xpBlockPlant
		}

		ExperienceAPI.addXP(player, "herbalism", xpValue)
	}

	suspend fun doQuickHarvestOnCrop(player: Player, block: Block, type: Material, fortuneLevel: Int) {
		if (!player.isValid) // Se o player saiu, cancele o quick harvest
			return

		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return

		if (block.type != type)
			return

		if (!player.canBreakAt(block.location, type))
			return

		if (useRadiusHarvest) {
			for (x in block.x - 7..block.x + 7) {
				for (z in block.z - 7..block.z + 7) {
					val farmBlock = block.world.getBlockAt(x, block.y, z)

					if (farmBlock.location.isChunkLoaded && farmBlock.type == type && player.canBreakAt(farmBlock.location, type)) {
						val damage = farmBlock.data

						val fullyGrown = when (type) {
							Material.MELON -> true
							Material.PUMPKIN -> true
							Material.NETHER_WART -> damage == 3.toByte()
							Material.BEETROOTS -> damage == 3.toByte()
							else -> damage == 7.toByte()
						}

						if (!fullyGrown)
							continue

						val itemStack = ItemStack(
							when (type) {
								Material.WHEAT -> Material.WHEAT
								Material.NETHER_WART -> Material.NETHER_WART
								Material.CARROTS -> Material.CARROT
								Material.POTATOES -> Material.POTATO
								Material.BEETROOTS -> Material.BEETROOT
								else -> type
							},
							when (type) {
								Material.WHEAT -> 1
								Material.NETHER_WART -> DreamUtils.random.nextInt(2, 5 + fortuneLevel)
								Material.CARROTS -> DreamUtils.random.nextInt(1, 5)
								Material.POTATOES -> DreamUtils.random.nextInt(1, 5)
								Material.BEETROOTS -> DreamUtils.random.nextInt(1, 5)
								else -> 1
							}
						)

						if (!player.inventory.canHoldItem(itemStack)) {
							player.sendTitle(
								"",
								"§cVocê está com o inventário cheio!",
								0,
								60,
								10
							)
							return
						}

						player.inventory.addItem(itemStack)

						if (type == Material.WHEAT) { // Trigo dropa seeds junto com a wheat, então vamos dropar algumas seeds aleatórias
							val seed = DreamUtils.random.nextInt(0, 4)
							if (seed != 0) {
								val seedItemStack = ItemStack(Material.WHEAT_SEEDS, seed)

								if (player.inventory.canHoldItem(seedItemStack)) {
									player.inventory.addItem(seedItemStack)
								} else {
									player.sendTitle(
										"",
										"§cVocê está com o inventário cheio!",
										0,
										60,
										10
									)
									return
								}
							}
						}

						val changeTo = when (type) {
							Material.PUMPKIN, Material.MELON -> Material.AIR
							else -> type
						}

						farmBlock.type = changeTo
						if (type != Material.MELON && type != Material.PUMPKIN) {
							val ageable = farmBlock.blockData as Ageable
							ageable.age = 0
							farmBlock.blockData = ageable
						}

						giveMcMMOHerbalismXP(player, block, type) // mcMMO EXP

						player.world.spawnParticle(Particle.VILLAGER_HAPPY, farmBlock.location.add(0.5, 0.5, 0.5), 3, 0.5, 0.5, 0.5)
					}
				}
			}
		} else {
			val damage = block.data

			val fullyGrown = when (type) {
				Material.MELON -> true
				Material.PUMPKIN -> true
				Material.NETHER_WART -> damage == 3.toByte()
				Material.BEETROOTS -> damage == 3.toByte()
				else -> damage == 7.toByte()
			}

			if (!fullyGrown)
				return

			val itemStack = ItemStack(
				when (type) {
					Material.WHEAT -> Material.WHEAT
					Material.NETHER_WART -> Material.NETHER_WART
					Material.CARROTS -> Material.CARROT
					Material.POTATOES -> Material.POTATO
					Material.BEETROOTS -> Material.BEETROOT
					else -> type
				},
				when (type) {
					Material.WHEAT -> 1
					Material.NETHER_WART -> DreamUtils.random.nextInt(2, 5 + fortuneLevel)
					Material.CARROTS -> DreamUtils.random.nextInt(1, 5)
					Material.POTATOES -> DreamUtils.random.nextInt(1, 5)
					Material.BEETROOTS -> DreamUtils.random.nextInt(1, 5)
					else -> 1
				}
			)

			if (!player.inventory.canHoldItem(itemStack))
				return

			player.inventory.addItem(itemStack)

			if (type == Material.WHEAT) { // Trigo dropa seeds junto com a wheat, então vamos dropar algumas seeds aleatórias
				val seed = DreamUtils.random.nextInt(0, 4)
				if (seed != 0) {
					val seedItemStack = ItemStack(Material.WHEAT_SEEDS, seed)

					if (player.inventory.canHoldItem(seedItemStack)) {
						player.inventory.addItem(seedItemStack)
					} else {
						block.world.dropItemNaturally(block.location, seedItemStack)
					}
				}
			}

			val changeTo = when (type) {
				Material.PUMPKIN, Material.MELON -> Material.AIR
				else -> type
			}

			block.type = changeTo
			if (type != Material.MELON && type != Material.PUMPKIN) {
				val ageable = block.blockData as Ageable
				ageable.age = 0
				block.blockData = ageable
			}

			giveMcMMOHerbalismXP(player, block, type) // mcMMO EXP

			player.world.spawnParticle(Particle.VILLAGER_HAPPY, block.location.add(0.5, 0.5, 0.5), 3, 0.5, 0.5, 0.5)

			delay(100)
			doQuickHarvestOnCrop(player, block.getRelative(BlockFace.NORTH), type, fortuneLevel)
			doQuickHarvestOnCrop(player, block.getRelative(BlockFace.SOUTH), type, fortuneLevel)
			doQuickHarvestOnCrop(player, block.getRelative(BlockFace.EAST), type, fortuneLevel)
			doQuickHarvestOnCrop(player, block.getRelative(BlockFace.WEST), type, fortuneLevel)
		}
	}

	suspend fun doQuickHarvestOnCocoa(e: BlockBreakEvent, player: Player, block: Block) {
		if (!player.isValid) // Se o player saiu, cancele o quick harvest
			return

		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return

		if (block.type != Material.COCOA)
			return

		if (!player.canBreakAt(block.location, block.type))
			return

		val itemStack = ItemStack(Material.COCOA_BEANS, DreamUtils.random.nextInt(2, 4))

		if (!player.inventory.canHoldItem(itemStack))
			return

		player.inventory.addItem(itemStack)

		val rotation = block.data and 3

		val stage = block.data and 12

		if (stage != 8.toByte()) // CocoaPlant.class
			return

		val blockage = block.blockData as Ageable
		blockage.age = 0
		block.blockData = blockage

		player.world.spawnParticle(Particle.VILLAGER_HAPPY, block.location.add(0.5, 0.5, 0.5), 3, 0.5, 0.5, 0.5)

		delay(100)

		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.NORTH))
		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.SOUTH))
		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.EAST))
		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.WEST))
		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.NORTH_EAST))
		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.NORTH_WEST))
		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.SOUTH_EAST))
		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.SOUTH_WEST))
		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.UP))
		doQuickHarvestOnCocoa(e, player, block.getRelative(BlockFace.DOWN))

		giveMcMMOHerbalismXP(player, block) // mcMMO EXP
	}

	suspend fun doQuickHarvestOnSugarCane(e: BlockBreakEvent, player: Player, block: Block) {
		if (!player.isValid) // Se o player saiu, cancele o quick harvest
			return

		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return

		if (block.type != Material.SUGAR_CANE)
			return

		if (!player.canBreakAt(block.location, block.type))
			return

		e.isCancelled = true

		// Pegar a posição no topo da sugar cane
		var top = block

		while (top.getRelative(BlockFace.UP).type == Material.SUGAR_CANE)
			top = top.getRelative(BlockFace.UP)

		// E ir destruindo para baixo!
		var bottom = top

		while (bottom.type == Material.SUGAR_CANE && bottom.getRelative(BlockFace.DOWN).type == Material.SUGAR_CANE) {
			val itemStack = ItemStack(
				Material.SUGAR_CANE, 1
			)

			if (!player.inventory.canHoldItem(itemStack))
				return

			player.inventory.addItem(itemStack)

			giveMcMMOHerbalismXP(player, bottom) // mcMMO EXP

			bottom.type = Material.AIR

			player.world.spawnParticle(Particle.VILLAGER_HAPPY, bottom.location.add(0.5, 0.5, 0.5), 3, 0.5, 0.5, 0.5)

			bottom = bottom.getRelative(BlockFace.DOWN)
		}

		delay(100)

		doQuickHarvestOnSugarCane(e, player, bottom.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH))
		doQuickHarvestOnSugarCane(e, player, bottom.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH))
		doQuickHarvestOnSugarCane(e, player, bottom.getRelative(BlockFace.UP).getRelative(BlockFace.EAST))
		doQuickHarvestOnSugarCane(e, player, bottom.getRelative(BlockFace.UP).getRelative(BlockFace.WEST))
		doQuickHarvestOnSugarCane(e, player, bottom.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST))
		doQuickHarvestOnSugarCane(e, player, bottom.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST))
		doQuickHarvestOnSugarCane(e, player, bottom.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST))
		doQuickHarvestOnSugarCane(e, player, bottom.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST))
	}
}