package net.perfectdreams.dreamquickharvest

import com.gmail.nossr50.datatypes.experience.XPGainReason
import com.gmail.nossr50.datatypes.experience.XPGainSource
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.datatypes.skills.SubSkillType
import com.gmail.nossr50.mcMMO
import com.gmail.nossr50.util.Permissions
import com.gmail.nossr50.util.player.UserManager
import com.gmail.nossr50.util.random.RandomChanceSkill
import com.gmail.nossr50.util.random.RandomChanceUtil
import dev.forst.exposed.insertOrUpdate
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import me.ryanhamshire.GriefPrevention.Claim
import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreammochilas.utils.MochilaAccessHolder
import net.perfectdreams.dreammochilas.utils.MochilaInventoryHolder
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import net.perfectdreams.dreamquickharvest.commands.declarations.ColheitaCommand
import net.perfectdreams.dreamquickharvest.tables.PlayerQuickHarvestData
import net.perfectdreams.dreamquickharvest.tables.PlayerQuickHarvestUpgrades
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Directional
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventoryPlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.experimental.and

class DreamQuickHarvest : KotlinPlugin(), Listener {
	companion object {
		private const val NO_HARVEST_BLOCKS_LEFT = "§cVocê já usou todos os seus blocos de colheita rápida... Espere ela recarregar! Use §6/colheita§c para saber mais informações sobre o sistema de colheita rápida e como evoluir!"
		const val DEFAULT_BLOCKS = 1_000
		const val HERBALISM_LEVEL_CAP = 10_000
		val BLOCK_ENERGY_COST = mapOf(
			Material.NETHER_WART to 1,
			Material.COCOA_BEANS to 1,
			Material.WHEAT to 2,
			Material.POTATOES to 2,
			Material.CARROTS to 2,
			Material.BEETROOT to 2,
			Material.SUGAR_CANE to 3,
			Material.MELON to 4,
			Material.PUMPKIN to 4
		)
	}

	private val harvestingMutexes = mutableMapOf<Player, Mutex>()

	override fun softEnable() {
		super.softEnable()

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(
				PlayerQuickHarvestData,
				PlayerQuickHarvestUpgrades
			)
		}

		val energySumField = PlayerQuickHarvestUpgrades.energy.sum()

		registerEvents(this)
		registerCommand(ColheitaCommand(this))

		launchAsyncThread {
			while (true) {
				val now = Clock.System.now()

				logger.info("Creating new thread to process energy... Now: $now")

				launchAsyncThread {
					val s = System.currentTimeMillis()
					val jobs = Bukkit.getOnlinePlayers().map { player ->
						launchAsyncThreadDeferred {
							lockAndRunIfDataExists(player) { info ->
								val maxUserBlocks = DEFAULT_BLOCKS + transaction(Databases.databaseNetwork) {
									PlayerQuickHarvestUpgrades.slice(energySumField).select {
										PlayerQuickHarvestUpgrades.playerId eq player.uniqueId and (PlayerQuickHarvestUpgrades.expiresAt greater now.toJavaInstant())
									}.firstOrNull()?.get(energySumField)
										?: 0 // Should NEVER be null if a row is present
								}

								if (info.activeBlocks != maxUserBlocks) {
									val newBlocks = howManyQuickHarvestBlocksThePlayerShouldEarn(player)
									info.activeBlocks += newBlocks

									logger.info { "Recharging ${player.name} with $newBlocks blocks, they have a max of $maxUserBlocks blocks!" }

									if (info.activeBlocks >= maxUserBlocks) {
										info.activeBlocks = maxUserBlocks
										player.sendActionBar(
											Component.empty()
												.color(NamedTextColor.GREEN)
												.append(Component.text("Suas energias sistema de colheita rápida foram recarregados!"))
										)
										player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
									} else {
										player.sendActionBar(
											Component.empty()
												.color(NamedTextColor.GREEN)
												.append(Component.text("Recarregando Energia... "))
												.append(
													Component.text("${info.activeBlocks}")
														.color(NamedTextColor.YELLOW)
												)
												.append(Component.text("/"))
												.append(
													Component.text("$maxUserBlocks")
														.color(NamedTextColor.YELLOW)
												)
										)
									}
								}
							}
						}
					}
					jobs.awaitAll()
					logger.info("Took ${System.currentTimeMillis() - s}ms to process energy!")
				}

				delay(1_000) // every second
			}
		}
	}

	fun howManyQuickHarvestBlocksThePlayerShouldEarn(player: Player): Int {
		val mcMMOPlayer = UserManager.getPlayer(player)

		val herbalismLevel = mcMMOPlayer?.herbalismManager?.skillLevel ?: 0

		// If the player has 1000 herbalism level, it should recharge 250 blocks per second
		// However it would be capped at level 1000
		return (5 + (herbalismLevel.coerceAtMost(HERBALISM_LEVEL_CAP) * 0.2)).toInt()
	}

	@EventHandler
	fun onQuit(e: PlayerQuitEvent) {
		harvestingMutexes.remove(e.player)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

			launchMainThread {
				lockAndRun(e.player) { info ->
					val (inventoryTarget, mochilaItem, mochila) = getInventoryTarget(
						e,
						"${e.player.name} harvesting crops"
					)

					val ttl = System.currentTimeMillis()
					val mcMMOXp = AtomicInteger()
					// Optimization: This is from McMMO's com.gmail.nossr50.util.BlockUtils.checkDoubleDrops, but we have "inlined" the getDoubleDropsEnabled and isSubSkillEnabled check for when the player attempts to harvest something, this way we avoid multiple repeating checks
					val executeDoubleDropsCheck = mcMMO.p.generalConfig.getDoubleDropsEnabled(PrimarySkillType.HERBALISM, e.block.type) && Permissions.isSubSkillEnabled(e.player, SubSkillType.HERBALISM_DOUBLE_DROPS)
					// Optimization: Create a RandomChanceSkill only once
					val randomChanceSkill = RandomChanceSkill(e.player, SubSkillType.HERBALISM_DOUBLE_DROPS, true)
					// Optimization: Get the current GriefPrevention claim of the clicked block, we are going to reuse it in the canBreak checks to avoid checking all claims
					val claim = GriefPrevention.instance.dataStore.getClaimAt(e.block.location, false, null)
					doQuickHarvestOnCrop(
						e.block,
						e.player,
						e.block.type,
						e.player.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS),
						inventoryTarget,
						mcMMOXp,
						info,
						AtomicBoolean(false),
						mutableSetOf(),
						executeDoubleDropsCheck,
						randomChanceSkill,
						claim
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
				lockAndRun(e.player) { info ->
					val (inventoryTarget, mochilaItem, mochila) = getInventoryTarget(
						e,
						"${e.player.name} harvesting cocoa"
					)

					val ttl = System.currentTimeMillis()
					val mcMMOXp = AtomicInteger()
					// Optimization: This is from McMMO's com.gmail.nossr50.util.BlockUtils.checkDoubleDrops, but we have "inlined" the getDoubleDropsEnabled and isSubSkillEnabled check for when the player attempts to harvest something, this way we avoid multiple repeating checks
					val executeDoubleDropsCheck = mcMMO.p.generalConfig.getDoubleDropsEnabled(PrimarySkillType.HERBALISM, e.block.type) && Permissions.isSubSkillEnabled(e.player, SubSkillType.HERBALISM_DOUBLE_DROPS)
					// Optimization: Create a RandomChanceSkill only once
					val randomChanceSkill = RandomChanceSkill(e.player, SubSkillType.HERBALISM_DOUBLE_DROPS, true)
					// Optimization: Get the current GriefPrevention claim of the clicked block, we are going to reuse it in the canBreak checks to avoid checking all claims
					val claim = GriefPrevention.instance.dataStore.getClaimAt(e.block.location, false, null)
					doQuickHarvestOnCocoa(e, e.player, e.block, inventoryTarget, mcMMOXp, info, AtomicBoolean(false), mutableSetOf(), executeDoubleDropsCheck, randomChanceSkill, claim)
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
				lockAndRun(e.player) { info ->
					val (inventoryTarget, mochilaItem, mochila) = getInventoryTarget(
						e,
						"${e.player.name} harvesting sugar cane"
					)

					val ttl = System.currentTimeMillis()
					val mcMMOXp = AtomicInteger()
					// Optimization: This is from McMMO's com.gmail.nossr50.util.BlockUtils.checkDoubleDrops, but we have "inlined" the getDoubleDropsEnabled and isSubSkillEnabled check for when the player attempts to harvest something, this way we avoid multiple repeating checks
					val executeDoubleDropsCheck = mcMMO.p.generalConfig.getDoubleDropsEnabled(PrimarySkillType.HERBALISM, e.block.type) && Permissions.isSubSkillEnabled(e.player, SubSkillType.HERBALISM_DOUBLE_DROPS)
					// Optimization: Create a RandomChanceSkill only once
					val randomChanceSkill = RandomChanceSkill(e.player, SubSkillType.HERBALISM_DOUBLE_DROPS, true)
					// Optimization: Get the current GriefPrevention claim of the clicked block, we are going to reuse it in the canBreak checks to avoid checking all claims
					val claim = GriefPrevention.instance.dataStore.getClaimAt(e.block.location, false, null)
					doQuickHarvestOnSugarCane(e, e.player, e.block, inventoryTarget, mcMMOXp, info, AtomicBoolean(false), executeDoubleDropsCheck, randomChanceSkill, claim)
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

	private suspend fun lockAndRun(player: Player, block: suspend (info: PlayerQuickHarvestInfo) -> (Unit)) {
		val mutex = harvestingMutexes.getOrPut(player) { Mutex() }
		mutex.withLock {
			loadAndUpdateUserEnergy(player) {
				block.invoke(it)
			}
		}
	}

	private suspend fun lockAndRunIfDataExists(player: Player, block: suspend (info: PlayerQuickHarvestInfo) -> (Unit)) {
		val mutex = harvestingMutexes.getOrPut(player) { Mutex() }
		mutex.withLock {
			loadAndUpdateUserEnergyIfExists(player) {
				block.invoke(it)
			}
		}
	}

	suspend fun loadAndUpdateUserEnergy(player: Player, block: suspend (info: PlayerQuickHarvestInfo) -> (Unit)) {
		val currentEnergy = onAsyncThread {
			transaction(Databases.databaseNetwork) {
				PlayerQuickHarvestData.select {
					PlayerQuickHarvestData.id eq player.uniqueId
				}.firstOrNull()?.get(PlayerQuickHarvestData.energy) ?: DEFAULT_BLOCKS
			}
		}

		val info = PlayerQuickHarvestInfo(currentEnergy)
		val newInfo = info.copy()

		block.invoke(newInfo)

		if (info != newInfo) {
			// Update user data if needed
			onAsyncThread {
				transaction(Databases.databaseNetwork) {
					PlayerQuickHarvestData.insertOrUpdate(PlayerQuickHarvestData.id) {
						it[PlayerQuickHarvestData.id] = player.uniqueId
						it[PlayerQuickHarvestData.energy] = newInfo.activeBlocks
					}
				}
			}
		}
	}

	suspend fun loadAndUpdateUserEnergyIfExists(player: Player, block: suspend (info: PlayerQuickHarvestInfo) -> (Unit)) {
		val ce = System.currentTimeMillis()

		val currentEnergy = onAsyncThread {
			transaction(Databases.databaseNetwork) {
				PlayerQuickHarvestData.select {
					PlayerQuickHarvestData.id eq player.uniqueId
				}.firstOrNull()?.get(PlayerQuickHarvestData.energy)
			}
		} ?: return

		val info = PlayerQuickHarvestInfo(currentEnergy)
		val newInfo = info.copy()

		block.invoke(newInfo)

		if (info != newInfo) {
			// Update user data if needed
			onAsyncThread {
				transaction(Databases.databaseNetwork) {
					PlayerQuickHarvestData.insertOrUpdate(PlayerQuickHarvestData.id) {
						it[PlayerQuickHarvestData.id] = player.uniqueId
						it[PlayerQuickHarvestData.energy] = newInfo.activeBlocks
					}
				}
			}
		}
	}

	private suspend fun getInventoryTarget(e: BlockBreakEvent, triggerType: String): InventoryTargetResult {
		var inventoryTarget: Inventory = e.player.inventory
		var mochila: MochilaAccessHolder? = null
		val item = e.player.inventory.itemInMainHand

		if (MochilaUtils.isMochila(item)) {
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
		if (mcMMO.getPlaceStore().isTrue(block)) // Optimization (albeit very smol): No need to get a BlockState instance, mcMMO allows us to pass a block instance instead
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

	fun doQuickHarvestOnCrop(
		startingBlock: Block,
		player: Player,
		type: Material,
		fortuneLevel: Int,
		inventory: Inventory,
		mcMMOXp: AtomicInteger,
		info: PlayerQuickHarvestInfo,
		playerHasBeenWarned: AtomicBoolean,
		checkedBlocks: MutableSet<Block>,
		executeDoubleDropsCheck: Boolean,
		randomChanceSkill: RandomChanceSkill,
		cachedClaim: Claim?
	) {
		// Optimization: Instead of being a recursive function, use a stack
		val blocksToBeChecked = ArrayDeque<Block>(1024)
		blocksToBeChecked.add(startingBlock)

		while (blocksToBeChecked.isNotEmpty()) {
			val block = blocksToBeChecked.pop() // Pop!

			checkedBlocks.add(block)

			if (!player.isValid) // Se o player saiu, cancele o quick harvest
				return

			if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
				continue

			if (block.type != type)
				continue

			if (!canBreakAt(block.location, player, type, cachedClaim))
				continue

			// A gente deixa na mesma altitude porque não tem problema se está tudo no mesmo chunk
			val distance = player.location.distanceSquared(block.location.apply { this.y = player.location.y })
			// Player está distante, 2304 = 48 ^ 2
			if (distance > 2304)
				continue

			val damage = block.data

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
				getOriginalStackCountOrDoubleIfUserHasHerbalismDoubleDropsChance(
					player,
					block.state,
					when (type) {
						Material.WHEAT -> 1
						Material.NETHER_WART -> DreamUtils.random.nextInt(2, 5 + fortuneLevel)
						Material.CARROTS -> DreamUtils.random.nextInt(1, 5)
						Material.POTATOES -> DreamUtils.random.nextInt(1, 5)
						Material.BEETROOTS -> 1
						else -> 1
					},
					executeDoubleDropsCheck,
					randomChanceSkill
				)
			)

			if (!inventory.canHoldItem(itemStack)) {
				sendInventoryFullTitle(player)
				return
			}

			if (removePlayerEnergyIfTheyHaveAndIfTheyDontSendMessage(player, info, block.type))
				return

			inventory.addItem(itemStack)

			// To avoid a duplication issue, we will change the type right now
			// Dupe issue: Fill your inventory with wheat, keep clicking on the wheat: You will receive a wheat item but the block won't be changed because
			// "I can't fit the seed in there!"
			if (type == Material.MELON || type == Material.PUMPKIN) {
				block.setType(Material.AIR, false) // Optimization: Do not apply physics when updating the block

				// However, this for melon and pumpkins, we need to check the blocks around the pumpkin to reset the stem!
				val possibleStemBlocks = listOf(
					block.getRelative(BlockFace.NORTH),
					block.getRelative(BlockFace.SOUTH),
					block.getRelative(BlockFace.EAST),
					block.getRelative(BlockFace.WEST),
				)

				for (possibleStemBlock in possibleStemBlocks) {
					if (type == Material.PUMPKIN && possibleStemBlock.type == Material.ATTACHED_PUMPKIN_STEM) {
						// Are we attached to the current block?
						val directional = (possibleStemBlock.blockData as Directional)
						if (possibleStemBlock.getRelative(directional.facing) == block) {
							// Reset to a fully grown pumpking stem
							val newStemData = Bukkit.createBlockData(Material.PUMPKIN_STEM) as Ageable
							newStemData.age = newStemData.maximumAge
							possibleStemBlock.setBlockData(newStemData, false)
						}
						break
					}

					if (type == Material.MELON && possibleStemBlock.type == Material.ATTACHED_MELON_STEM) {
						// Are we attached to the current block?
						val directional = (possibleStemBlock.blockData as Directional)
						if (possibleStemBlock.getRelative(directional.facing) == block) {
							// Reset to a fully grown pumpking stem
							val newStemData = Bukkit.createBlockData(Material.MELON_STEM) as Ageable
							newStemData.age = newStemData.maximumAge
							possibleStemBlock.setBlockData(newStemData, false)
						}
						break
					}
				}
			} else {
				val ageable = block.blockData as Ageable
				ageable.age = 0
				block.setBlockData(ageable, false) // Optimization: Do not apply physics when updating the block
			}

			if (type == Material.WHEAT) { // Trigo dropa seeds junto com a wheat, então vamos dropar algumas seeds aleatórias
				val seed = getOriginalStackCountOrDoubleIfUserHasHerbalismDoubleDropsChance(
					player,
					block.state,
					DreamUtils.random.nextInt(0, 4),
					executeDoubleDropsCheck,
					randomChanceSkill
				)

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

			addMcMMOHerbalismXP(player, block, type, mcMMOXp) // mcMMO EXP

			player.world.spawnParticle(
				Particle.VILLAGER_HAPPY,
				block.location.add(0.5, 0.5, 0.5),
				3,
				0.5,
				0.5,
				0.5
			)

			// Optimization: If the additional blocks are only used in pumpkin and melon farms, then only check them FOR pumpkin and melon farms!
			// This way we can avoid additional "useless" overhead on the for each down below
			val blocksThatMustBeHarvestedLater = if (type != Material.PUMPKIN && type != Material.MELON)
				listOf(
					block.getRelative(BlockFace.NORTH),
					block.getRelative(BlockFace.SOUTH),
					block.getRelative(BlockFace.EAST),
					block.getRelative(BlockFace.WEST)
				)
			else {
				listOf(
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
			}

			// Optimization: Don't use .sortedBy { startingBlock.location.distanceSquared(it.location) }
			// Yeah, it looks prettier, but avoiding calling distanceSquared is better :3
			for (it in blocksThatMustBeHarvestedLater) {
				if (playerHasBeenWarned.get())
					return

				if (doesPlayerNotHaveEnoughEnergyToHarvestIfTheyDontSendMessage(player, info, type)) {
					playerHasBeenWarned.set(true)
					return
				}

				// This block was already checked, so let's bail out
				if (it in checkedBlocks)
					continue

				blocksToBeChecked.add(it)
			}
		}
	}

	private fun doQuickHarvestOnCocoa(
		e: BlockBreakEvent,
		player: Player,
		block: Block,
		inventory: Inventory,
		mcMMOXp: AtomicInteger,
		info: PlayerQuickHarvestInfo,
		playerHasBeenWarned: AtomicBoolean,
		checkedBlocks: MutableSet<Block>,
		executeDoubleDropsCheck: Boolean,
		randomChanceSkill: RandomChanceSkill,
		cachedClaim: Claim?
	) {
		// This block was already checked, so let's bail out
		if (block in checkedBlocks)
			return

		checkedBlocks.add(block)

		if (!player.isValid) // Se o player saiu, cancele o quick harvest
			return

		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return

		if (block.type != Material.COCOA)
			return

		if (!canBreakAt(block.location, player, block.type, cachedClaim))
			return

		// A gente deixa na mesma altitude porque não tem problema se está tudo no mesmo chunk
		val distance = player.location.distanceSquared(block.location.apply { this.y = player.location.y })

		// Player está distante, 2304 = 48 ^ 2
		if (distance > 2304)
			return

		val blockage = block.blockData as Ageable

		if (blockage.age != blockage.maximumAge)
			return

		val itemStack = ItemStack(
			Material.COCOA_BEANS,
			getOriginalStackCountOrDoubleIfUserHasHerbalismDoubleDropsChance(
				player,
				block.state,
				DreamUtils.random.nextInt(2, 4),
				executeDoubleDropsCheck,
				randomChanceSkill
			)
		)

		if (!inventory.canHoldItem(itemStack)) {
			sendInventoryFullTitle(player)
			return
		}

		if (removePlayerEnergyIfTheyHaveAndIfTheyDontSendMessage(player, info, block.type))
			return

		inventory.addItem(itemStack)

		addMcMMOHerbalismXP(player, block, mcMMOXp = mcMMOXp) // mcMMO EXP

		blockage.age = 0
		block.blockData = blockage

		player.world.spawnParticle(Particle.VILLAGER_HAPPY, block.location.add(0.5, 0.5, 0.5), 3, 0.5, 0.5, 0.5)

		val blocksThatMustBeHarvestedLater = listOf(
			block.getRelative(BlockFace.NORTH),
			block.getRelative(BlockFace.SOUTH),
			block.getRelative(BlockFace.EAST),
			block.getRelative(BlockFace.WEST),
			block.getRelative(BlockFace.UP),
			block.getRelative(BlockFace.DOWN)
		)

		// Optimization: Don't use .sortedBy { startingBlock.location.distanceSquared(it.location) }
		// Yeah, it looks prettier, but avoiding calling distanceSquared is better :3
		blocksThatMustBeHarvestedLater.forEach {
			if (playerHasBeenWarned.get())
				return

			if (doesPlayerNotHaveEnoughEnergyToHarvestIfTheyDontSendMessage(player, info, Material.COCOA)) {
				playerHasBeenWarned.set(true)
				return
			}

			doQuickHarvestOnCocoa(
				e,
				player,
				it,
				inventory,
				mcMMOXp,
				info,
				playerHasBeenWarned,
				checkedBlocks,
				executeDoubleDropsCheck,
				randomChanceSkill,
				cachedClaim
			)
		}
	}

	private fun doQuickHarvestOnSugarCane(
		e: BlockBreakEvent,
		player: Player,
		block: Block,
		inventory: Inventory,
		mcMMOXp: AtomicInteger,
		info: PlayerQuickHarvestInfo,
		playerHasBeenWarned: AtomicBoolean,
		executeDoubleDropsCheck: Boolean,
		randomChanceSkill: RandomChanceSkill,
		cachedClaim: Claim?
	) {
		if (!player.isValid) // Se o player saiu, cancele o quick harvest
			return

		if (!block.chunk.isLoaded) // Se o chunk não está carregado, ignore, não vamos carregar ele apenas para fazer quick harvest
			return

		if (block.type != Material.SUGAR_CANE)
			return

		if (!canBreakAt(block.location, player, block.type, cachedClaim))
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
				Material.SUGAR_CANE,
				getOriginalStackCountOrDoubleIfUserHasHerbalismDoubleDropsChance(
					player,
					block.state,
					1,
					executeDoubleDropsCheck,
					randomChanceSkill
				)
			)

			if (!inventory.canHoldItem(itemStack)) {
				sendInventoryFullTitle(player)
				return
			}

			if (removePlayerEnergyIfTheyHaveAndIfTheyDontSendMessage(player, info, bottom.type))
				return

			inventory.addItem(itemStack)

			addMcMMOHerbalismXP(player, bottom, mcMMOXp = mcMMOXp) // mcMMO EXP

			bottom.type = Material.AIR

			player.world.spawnParticle(Particle.VILLAGER_HAPPY, bottom.location.add(0.5, 0.5, 0.5), 3, 0.5, 0.5, 0.5)

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

		// Optimization: Don't use .sortedBy { startingBlock.location.distanceSquared(it.location) }
		// Yeah, it looks prettier, but avoiding calling distanceSquared is better :3
		blocksThatMustBeHarvestedLater.forEach {
			if (playerHasBeenWarned.get())
				return

			if (doesPlayerNotHaveEnoughEnergyToHarvestIfTheyDontSendMessage(player, info, Material.SUGAR_CANE)) {
				playerHasBeenWarned.set(true)
				return
			}

			doQuickHarvestOnSugarCane(
				e,
				player,
				it,
				inventory,
				mcMMOXp,
				info,
				playerHasBeenWarned,
				executeDoubleDropsCheck,
				randomChanceSkill,
				cachedClaim
			)
		}
	}

	private fun doesPlayerNotHaveEnoughEnergyToHarvestType(info: PlayerQuickHarvestInfo, type: Material): Boolean {
		val howMuchWillBeRemoved = BLOCK_ENERGY_COST[type] ?: 1
		if (0 >= info.activeBlocks - howMuchWillBeRemoved)
			return true
		return false
	}

	private fun doesPlayerNotHaveEnoughEnergyToHarvestIfTheyDontSendMessage(player: Player, info: PlayerQuickHarvestInfo, type: Material): Boolean {
		if (doesPlayerNotHaveEnoughEnergyToHarvestType(info, type)) {
			player.sendMessage(NO_HARVEST_BLOCKS_LEFT)
			return true
		}
		return false
	}

	private fun removePlayerEnergyIfTheyHaveAndIfTheyDontSendMessage(player: Player, info: PlayerQuickHarvestInfo, type: Material): Boolean {
		val howMuchWillBeRemoved = BLOCK_ENERGY_COST[type] ?: 1
		if (doesPlayerNotHaveEnoughEnergyToHarvestType(info, type)) {
			player.sendMessage(NO_HARVEST_BLOCKS_LEFT)
			return true
		}
		info.activeBlocks -= howMuchWillBeRemoved
		return false
	}

	private fun sendInventoryFullTitle(player: Player) {
		player.sendTitle(
			"§c",
			"§cVocê está com o inventário cheio!",
			0,
			60,
			10
		)
	}

	private fun getOriginalStackCountOrDoubleIfUserHasHerbalismDoubleDropsChance(
		player: Player,
		blockState: BlockState,
		stackCount: Int,
		executeDoubleDropsCheck: Boolean,
		randomChanceSkill: RandomChanceSkill
	): Int {
		return if (!executeDoubleDropsCheck || mcMMO.getPlaceStore().isTrue(blockState)) {
			// User placed the block
			stackCount
		} else {
			// Optimization: This is from McMMO's com.gmail.nossr50.util.BlockUtils.checkDoubleDrops, but we have "inlined" the getDoubleDropsEnabled and isSubSkillEnabled check for when the player attempts to harvest something, this way we avoid multiple repeating checks
			val hasDoubleDrops = RandomChanceUtil.checkRandomChanceExecutionSuccess(randomChanceSkill)

			if (hasDoubleDrops) {
				stackCount * 2
			} else {
				stackCount
			}
		}
	}

	data class PlayerQuickHarvestInfo(var activeBlocks: Int)

	fun canBreakAt(loc: Location, p: Player, m: Material, cachedClaim: Claim?): Boolean {
		val claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, cachedClaim)
		// Performance: https://github.com/TechFortress/GriefPrevention/issues/1438#issuecomment-872363793
		var canBuildClaim = true

		if (claim != null) // The supplier can be "null"!
			canBuildClaim = claim.checkPermission(p, ClaimPermission.Build, PlayerUtils.CompatBuildBreakEvent(m, true)) == null

		return canBuildClaim && WorldGuardUtils.canBreakAt(loc, p)
	}
}