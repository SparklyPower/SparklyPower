package net.perfectdreams.dreamquickharvest

import com.gmail.nossr50.datatypes.experience.XPGainReason
import com.gmail.nossr50.datatypes.experience.XPGainSource
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.mcMMO
import com.gmail.nossr50.util.player.UserManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.extensions.canBreakAt
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreammochilas.utils.MochilaAccessHolder
import net.perfectdreams.dreammochilas.utils.MochilaInventoryHolder
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import net.perfectdreams.dreamquickharvest.commands.ColheitaExecutor
import net.perfectdreams.dreamquickharvest.commands.ColheitaUpgradeExecutor
import net.perfectdreams.dreamquickharvest.commands.declarations.ColheitaCommand
import org.bukkit.Bukkit
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
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.experimental.and
import kotlinx.serialization.Serializable
import net.perfectdreams.dreamcore.utils.*
import org.bukkit.Sound

class DreamQuickHarvest : KotlinPlugin(), Listener {
	companion object {
		private const val ALREADY_FARMING = "§cVocê já está colhendo! Espere a colheita acabar para começar outra!"
		private const val NO_HARVEST_BLOCKS_LEFT = "§cVocê já usou todos os seus blocos de colheita rápida... Espere ela recarregar! Use §6/colheita§c para saber mais informações sobre o sistema de colheita rápida e como evoluir!"
		const val MAX_BLOCKS = 1000
		const val MAX_TOTAL_BLOCKS = 10_000
	}

	private val harvestingMutexes = mutableMapOf<Player, Mutex>()
	// TODO: Save on the database later :)
	private val playerHarvestBlocks = mutableMapOf<UUID, PlayerQuickHarvestInfo>()

	override fun softEnable() {
		super.softEnable()

		registerEvents(this)
		registerCommand(
			ColheitaCommand,
			ColheitaExecutor(this),
			ColheitaUpgradeExecutor(this)
		)

		launchMainThread {
			while (true) {
				for (player in Bukkit.getOnlinePlayers()) {
					val info = playerHarvestBlocks[player.uniqueId] ?: continue
					info.mutex.withLock {
						if (info.activeBlocks != MAX_BLOCKS) {
							val newBlocks = howManyQuickHarvestBlocksThePlayerShouldEarn(player)
							info.activeBlocks += newBlocks

							logger.info { "Recharging ${player.name} with ${newBlocks} blocks" }

							if (info.activeBlocks >= MAX_BLOCKS) {
								info.activeBlocks = MAX_BLOCKS
								player.sendActionBar(
									Component.empty()
										.color(NamedTextColor.GREEN)
										.append(Component.text("Seus blocos do sistema de colheita rápida foram recarregados!"))
								)
								player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
							} else {
								player.sendActionBar(
									Component.empty()
										.color(NamedTextColor.GREEN)
										.append(Component.text("Recarregando Blocos... "))
										.append(
											Component.text("${info.activeBlocks}")
												.color(NamedTextColor.YELLOW)
										)
										.append(Component.text("/"))
										.append(
											Component.text("$MAX_BLOCKS")
												.color(NamedTextColor.YELLOW)
										)
								)
							}
						}
					}
				}

				delay(1_000) // every second
			}
		}
	}

	override fun softDisable() {
		super.softDisable()
	}

	fun getOrCreateQuickHarvestInfo(player: Player) = playerHarvestBlocks.getOrPut(player.uniqueId) {
		PlayerQuickHarvestInfo(
			PlayerQuickHarvestData(MAX_BLOCKS),
			Mutex()
		)
	}

	fun howManyQuickHarvestBlocksThePlayerShouldEarn(player: Player): Int {
		val mcMMOPlayer = UserManager.getPlayer(player)

		val herbalismLevel = mcMMOPlayer?.herbalismManager?.skillLevel ?: 0

		// If the player has 1000 herbalism level, it should recharge 250 blocks per second
		// However it would be capped at level 1000
		return (5 + (herbalismLevel.coerceAtMost(1000) * 0.245)).toInt()
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		harvestingMutexes.remove(e.player)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onBreak(e: BlockBreakEvent) {
		if (e.player.isSneaking)
			return

		val plugin = this

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

			launchMainThread {
				runIfNotLocked(e.player) { info ->
					val (inventoryTarget, mochilaItem, mochila) = getInventoryTarget(
						e,
						"${e.player.name} harvesting crops"
					)

					val ttl = System.currentTimeMillis()
					val mcMMOXp = AtomicInteger()
					doQuickHarvestOnCrop(
						e.block,
						e.player,
						e.block,
						e.block.type,
						e.player.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS),
						inventoryTarget,
						mcMMOXp,
						info
					)
					giveMcMMOHerbalismXP(e.player, mcMMOXp)
					logger.info { "Took ${System.currentTimeMillis() - ttl}ms for ${e.player.name} to harvest normal crops!" }

					if (mochila != null) {
						onAsyncThread {
							// Let's unlock the mochila lock and the inventory lock!
							// The reason we use the inventory target holder to release, is because we don't really care about *what* access we are closing actually
							// If we were closing by using "mochila.release()", another part of the code may close the already closed MochilaAccessHolder, which causes issues!
							(inventoryTarget.holder as MochilaInventoryHolder).accessHolders.poll()
								?.release("${e.player.name} harvesting crops")
						}

						if (mochilaItem != null)
							MochilaUtils.updateMochilaItemLore(inventoryTarget, mochilaItem)
					}
				}
			}
			return
		}

		if (e.block.type == Material.COCOA) {
			if (!shouldCancelCocoaEvent(e, e.block))
				return

			e.isCancelled = true

			launchMainThread {
				runIfNotLocked(e.player) { info ->
					val (inventoryTarget, mochilaItem, mochila) = getInventoryTarget(
						e,
						"${e.player.name} harvesting cocoa"
					)

					val ttl = System.currentTimeMillis()
					val mcMMOXp = AtomicInteger()
					doQuickHarvestOnCocoa(e, e.player, e.block, inventoryTarget, mcMMOXp, info)
					giveMcMMOHerbalismXP(e.player, mcMMOXp)
					logger.info { "Took ${System.currentTimeMillis() - ttl}ms for ${e.player.name} to harvest cocoa!" }

					if (mochila != null) {
						onAsyncThread {
							// Let's unlock the mochila lock and the inventory lock!
							// The reason we use the inventory target holder to release, is because we don't really care about *what* access we are closing actually
							// If we were closing by using "mochila.release()", another part of the code may close the already closed MochilaAccessHolder, which causes issues!
							(inventoryTarget.holder as MochilaInventoryHolder).accessHolders.poll()
								?.release("${e.player.name} harvesting cocoa")
						}

						if (mochilaItem != null)
							MochilaUtils.updateMochilaItemLore(inventoryTarget, mochilaItem)
					}
				}
			}
			return
		}

		if (e.block.type == Material.SUGAR_CANE && (e.block.getRelative(BlockFace.DOWN).type == Material.SUGAR_CANE || e.block.getRelative(BlockFace.UP).type == Material.SUGAR_CANE)) { // Apenas execute o quick harvest caso seja uma plantação de sugar cane
			e.isCancelled = true

			launchMainThread {
				runIfNotLocked(e.player) { info ->
					val (inventoryTarget, mochilaItem, mochila) = getInventoryTarget(
						e,
						"${e.player.name} harvesting sugar cane"
					)

					val ttl = System.currentTimeMillis()
					val mcMMOXp = AtomicInteger()
					doQuickHarvestOnSugarCane(e, e.player, e.block, inventoryTarget, mcMMOXp, info)
					giveMcMMOHerbalismXP(e.player, mcMMOXp)
					logger.info { "Took ${System.currentTimeMillis() - ttl}ms for ${e.player.name} to harvest sugar canes!" }

					if (mochila != null) {
						onAsyncThread {
							// Let's unlock the mochila lock and the inventory lock!
							// The reason we use the inventory target holder to release, is because we don't really care about *what* access we are closing actually
							// If we were closing by using "mochila.release()", another part of the code may close the already closed MochilaAccessHolder, which causes issues!
							(inventoryTarget.holder as MochilaInventoryHolder).accessHolders.poll()
								?.release("${e.player.name} harvesting sugar cane")
						}

						if (mochilaItem != null)
							MochilaUtils.updateMochilaItemLore(inventoryTarget, mochilaItem)
					}
				}
			}
			return
		}
	}

	private suspend inline fun runIfNotLocked(player: Player, block: (info: PlayerQuickHarvestInfo) -> (Unit)) {
		try {
			harvestingMutexes.getOrPut(player) { Mutex() }.tryLock()

			val info = getOrCreateQuickHarvestInfo(player)

			info.mutex.withLock {
				block.invoke(info)
			}
		} catch (e: IllegalStateException) {
			// Mutex is locked, let's ignore it then...
			player.sendMessage(ALREADY_FARMING)
		}
	}

	suspend fun getInventoryTarget(e: BlockBreakEvent, triggerType: String): InventoryTargetResult {
		var inventoryTarget: Inventory = e.player.inventory
		var mochila: MochilaAccessHolder? = null
		val item = e.player.inventory.itemInMainHand

		if (item.type == Material.CARROT_ON_A_STICK) {
			val mochilaId = MochilaUtils.getMochilaId(item)

			if (mochilaId != null) {
				mochila = onAsyncThread {
					MochilaUtils.retrieveMochilaAndHold(mochilaId, triggerType)
				}

				if (mochila != null) {
					inventoryTarget = mochila.getOrCreateMochilaInventoryAndHold()
				}
			}
		}

		return InventoryTargetResult(inventoryTarget, item, mochila)
	}

	data class InventoryTargetResult(
		val inventoryTarget: Inventory,
		val mochilaItem: ItemStack?,
		val mochila: MochilaAccessHolder?
	)

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

	fun addMcMMOHerbalismXP(
		player: Player,
		block: Block,
		material: Material? = null,
		mcMMOXp: AtomicInteger
	) {
		if (mcMMO.getPlaceStore().isTrue(block.state))
			return

		var xpBlockPlant = 0

		if (material != null){
			xpBlockPlant = when (material) {
				Material.PUMPKIN -> 20
				Material.MELON -> 20
				else -> 0
			}
		}

		val xpValue = when (block.type) {
			Material.COCOA -> 30
			Material.SUGAR_CANE -> 30
			Material.NETHER_WART -> 50
			Material.BEETROOTS -> 50
			Material.POTATOES -> 50
			Material.CARROTS -> 50
			Material.WHEAT -> 50
			else -> xpBlockPlant
		}

		// Yes, there is a "ExperienceAPI.addXP" method, but it *sucks* because you can't pass a PrimarySkillType enum
		// But you may ask: "What is the issue in that?": McMMO creates a PATTERN AND A LOCALE just to check what skill type is,
		// bringing our sweet TPS down the drain
		//
		// So we add the XP directly via the "beginXpGain" method, which is what the "addXP" uses behind the scenes
		mcMMOXp.addAndGet(xpValue)
	}

	fun giveMcMMOHerbalismXP(
		player: Player,
		mcMMOXp: AtomicInteger
	) {
		// We accumulate all XP and then give the XP to the player
		// This avoids lag spikes due to mcMMO trying to add XP to the player on every single block they are breaking
		val xpToBeGiven = mcMMOXp.get()
		if (0 >= xpToBeGiven)
			return

		UserManager.getPlayer(player)?.beginXpGain(
			PrimarySkillType.HERBALISM,
			xpToBeGiven.toFloat(),
			XPGainReason.UNKNOWN,
			XPGainSource.CUSTOM
		)
	}

	suspend fun doQuickHarvestOnCrop(
		startingBlock: Block,
		player: Player,
		block: Block,
		type: Material,
		fortuneLevel: Int,
		inventory: Inventory,
		mcMMOXp: AtomicInteger,
		info: PlayerQuickHarvestInfo
	) {
		if (!player.isValid) // Se o player saiu, cancele o quick harvest
			return

		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return

		if (block.type != type)
			return

		if (!player.canBreakAt(block.location, type))
			return

		// A gente deixa na mesma altitude porque não tem problema se está tudo no mesmo chunk
		val distance = player.location.distanceSquared(block.location.apply { this.y = player.location.y })

		// Player está distante, 2304 = 48 ^ 2
		if (distance > 2304)
			return

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
				Material.BEETROOTS -> 1
				else -> 1
			}
		)

		if (!inventory.canHoldItem(itemStack)) {
			if (startingBlock == block)
				sendInventoryFullTitle(player)
			return
		}

		if (info.activeBlocks == 0) {
			player.sendMessage(NO_HARVEST_BLOCKS_LEFT)
			return
		}

		inventory.addItem(itemStack)

		if (type == Material.WHEAT) { // Trigo dropa seeds junto com a wheat, então vamos dropar algumas seeds aleatórias
			val seed = DreamUtils.random.nextInt(0, 4)
			if (seed != 0) {
				val seedItemStack = ItemStack(Material.WHEAT_SEEDS, seed)

				if (inventory.canHoldItem(seedItemStack)) {
					inventory.addItem(seedItemStack)
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

		block.type = changeTo
		if (type != Material.MELON && type != Material.PUMPKIN) {
			val ageable = block.blockData as Ageable
			ageable.age = 0
			block.blockData = ageable
		}

		addMcMMOHerbalismXP(player, block, type, mcMMOXp) // mcMMO EXP

		player.world.spawnParticle(
			Particle.VILLAGER_HAPPY,
			block.location.add(0.5, 0.5, 0.5),
			3,
			0.5,
			0.5,
			0.5
		)

		info.activeBlocks -= 1

		val blocksThatMustBeHarvestedLater = listOf(
			block.getRelative(BlockFace.NORTH),
			block.getRelative(BlockFace.SOUTH),
			block.getRelative(BlockFace.EAST),
			block.getRelative(BlockFace.WEST),
			// All of these are just for pumpkin/melon farms
			block.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH),
			block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH),
			block.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST),
			block.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST),
			block.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH),
			block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH),
			block.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST).getRelative(BlockFace.EAST),
			block.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST),
			block.getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH).getRelative(BlockFace.NORTH),
			block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH).getRelative(BlockFace.SOUTH),
			block.getRelative(BlockFace.EAST).getRelative(BlockFace.EAST).getRelative(BlockFace.EAST).getRelative(BlockFace.EAST),
			block.getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST).getRelative(BlockFace.WEST)
		)

		blocksThatMustBeHarvestedLater.sortedBy { startingBlock.location.distanceSquared(it.location) }.forEach {
			doQuickHarvestOnCrop(startingBlock, player, it, type, fortuneLevel, inventory, mcMMOXp, info)
			if (info.activeBlocks == 0)
				return
		}
	}

	suspend fun doQuickHarvestOnCocoa(
		e: BlockBreakEvent,
		player: Player,
		block: Block,
		inventory: Inventory,
		mcMMOXp: AtomicInteger,
		info: PlayerQuickHarvestInfo
	) {
		if (!player.isValid) // Se o player saiu, cancele o quick harvest
			return

		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return

		if (block.type != Material.COCOA)
			return

		if (!player.canBreakAt(block.location, block.type))
			return

		// A gente deixa na mesma altitude porque não tem problema se está tudo no mesmo chunk
		val distance = player.location.distanceSquared(block.location.apply { this.y = player.location.y })

		// Player está distante, 2304 = 48 ^ 2
		if (distance > 2304)
			return

		val itemStack = ItemStack(Material.COCOA_BEANS, DreamUtils.random.nextInt(2, 4))

		if (!inventory.canHoldItem(itemStack)) {
			if (e.block == block)
				sendInventoryFullTitle(player)
			return
		}

		inventory.addItem(itemStack)

		val blockage = block.blockData as Ageable

		if (blockage.age != blockage.maximumAge)
			return

		if (info.activeBlocks == 0) {
			player.sendMessage(NO_HARVEST_BLOCKS_LEFT)
			return
		}

		addMcMMOHerbalismXP(player, block, mcMMOXp = mcMMOXp) // mcMMO EXP

		blockage.age = 0
		block.blockData = blockage

		info.activeBlocks -= 1

		player.world.spawnParticle(Particle.VILLAGER_HAPPY, block.location.add(0.5, 0.5, 0.5), 3, 0.5, 0.5, 0.5)

		val blocksThatMustBeHarvestedLater = listOf(
			block.getRelative(BlockFace.NORTH),
			block.getRelative(BlockFace.SOUTH),
			block.getRelative(BlockFace.EAST),
			block.getRelative(BlockFace.WEST),
			block.getRelative(BlockFace.UP),
			block.getRelative(BlockFace.DOWN)
		)

		blocksThatMustBeHarvestedLater.sortedBy { e.block.location.distanceSquared(it.location) }.forEach {
			doQuickHarvestOnCocoa(e, player, it, inventory, mcMMOXp, info)
			if (info.activeBlocks == 0)
				return
		}
	}

	suspend fun doQuickHarvestOnSugarCane(
		e: BlockBreakEvent,
		player: Player,
		block: Block,
		inventory: Inventory,
		mcMMOXp: AtomicInteger,
		info: PlayerQuickHarvestInfo
	) {
		if (!player.isValid) // Se o player saiu, cancele o quick harvest
			return

		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return

		if (block.type != Material.SUGAR_CANE)
			return

		if (!player.canBreakAt(block.location, block.type))
			return

		// A gente deixa na mesma altitude porque não tem problema se está tudo no mesmo chunk
		val distance = player.location.distanceSquared(block.location.apply { this.y = player.location.y })

		// Player está distante, 2304 = 48 ^ 2
		if (distance > 2304)
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

			if (info.activeBlocks == 0) {
				player.sendMessage(NO_HARVEST_BLOCKS_LEFT)
				return
			}

			if (!inventory.canHoldItem(itemStack)) {
				if (e.block == bottom)
					sendInventoryFullTitle(player)
				return
			}

			inventory.addItem(itemStack)

			addMcMMOHerbalismXP(player, bottom, mcMMOXp = mcMMOXp) // mcMMO EXP

			bottom.type = Material.AIR

			player.world.spawnParticle(Particle.VILLAGER_HAPPY, bottom.location.add(0.5, 0.5, 0.5), 3, 0.5, 0.5, 0.5)

			info.activeBlocks -= 1

			bottom = bottom.getRelative(BlockFace.DOWN)
		}

		val blocksThatMustBeHarvestedLater = listOf(
			bottom.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH),
			bottom.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH),
			bottom.getRelative(BlockFace.UP).getRelative(BlockFace.EAST),
			bottom.getRelative(BlockFace.UP).getRelative(BlockFace.WEST),
			bottom.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST),
			bottom.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST),
			bottom.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST),
			bottom.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST)
		)

		blocksThatMustBeHarvestedLater.sortedBy { e.block.location.distanceSquared(it.location) }.forEach {
			doQuickHarvestOnSugarCane(e, player, it, inventory, mcMMOXp, info)
			if (info.activeBlocks == 0)
				return
		}
	}

	private fun sendInventoryFullTitle(player: Player) {
		player.sendTitle(
			"§cSabia que...",
			"§cVocê está com o inventário cheio!",
			0,
			60,
			10
		)
	}

	class PlayerQuickHarvestInfo(
		var data: PlayerQuickHarvestData,
		var mutex: Mutex,
	) {
		var activeBlocks by data::activeBlocks
	}

	@Serializable
	data class PlayerQuickHarvestData(
		var activeBlocks: Int
	)
}