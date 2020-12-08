package net.perfectdreams.dreampicaretamonstra

import com.gmail.nossr50.datatypes.player.McMMOPlayer
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.events.skills.repair.McMMOPlayerRepairCheckEvent
import com.gmail.nossr50.events.skills.salvage.McMMOPlayerSalvageCheckEvent
import com.gmail.nossr50.mcMMO
import com.gmail.nossr50.skills.mining.MiningManager
import com.gmail.nossr50.util.player.UserManager
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

class DreamPicaretaMonstra : KotlinPlugin(), Listener {
	companion object {
		lateinit var INSTANCE: DreamPicaretaMonstra
	}

	override fun softEnable() {
		super.softEnable()

		INSTANCE = this

		registerCommand(object: SparklyCommand(arrayOf("picaretamonstra"), permission = "sparklypower.picaretamonstra") {
			@Subcommand
			fun spawn(player: Player) {
				player.inventory.addItem(
						ItemStack(Material.DIAMOND_PICKAXE).storeMetadata("isMonsterPickaxe", "true")
								.rename("§6§lPicareta Monstra")
								.lore("§6Tá saindo da jaula o monstro!")
				)
			}
		})

		registerCommand(object: SparklyCommand(arrayOf("pamonstra"), permission = "sparklypower.pamonstra") {
			@Subcommand
			fun spawn(player: Player) {
				player.inventory.addItem(
					ItemStack(Material.DIAMOND_SHOVEL).storeMetadata("isMonsterPickaxe", "true")
						.rename("§6§lPá Monstra")
						.lore("§6Tá saindo da jaula o monstro!")
				)
			}
		})

		registerEvents(this)
	}

	fun isValidMiningBlock(block: Block): Boolean {
		return block.type.name.contains("ORE") || block.type === Material.STONE || block.type === Material.NETHERRACK || block.type == Material.GRANITE || block.type == Material.DIORITE || block.type == Material.ANDESITE
	}

	fun isValidShovellingBlock(block: Block): Boolean {
		return block.type == Material.GRASS_BLOCK || block.type == Material.DIRT || block.type == Material.SAND || block.type == Material.GRAVEL
	}

	fun isValidForHeldItem(material: Material, block: Block): Boolean {
		return if (material == Material.DIAMOND_PICKAXE)
			isValidMiningBlock(block)
		else isValidShovellingBlock(block)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onRepair(e: McMMOPlayerRepairCheckEvent) {
		if (e.repairedObject.getStoredMetadata("isMonsterPickaxe") == "true") {
			e.isCancelled = true
			e.player.sendMessage("§cVocê não pode reparar uma ferramenta monstra!")
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onSalvage(e: McMMOPlayerSalvageCheckEvent) {
		if (e.salvageItem.getStoredMetadata("isMonsterPickaxe") == "true") {
			e.isCancelled = true
			e.player.sendMessage("§cVocê não pode salvar uma ferramenta monstra!")
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onAnvilRepair(e: PrepareAnvilEvent) {
		val inventory = e.inventory
		if (e.result?.getStoredMetadata("isMonsterPickaxe") == "true")
			inventory.repairCost *= 16
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onPlayerDropItemEvent(e: PlayerDropItemEvent) {
		// Because some people are DUMB AS FUCC
		val droppedItem = e.itemDrop.itemStack

		if (droppedItem.getStoredMetadata("isMonsterPickaxe") != "true")
			return

		e.itemDrop.setMetadata(
			"owner",
			FixedMetadataValue(
				this,
				e.player.uniqueId
			)
		)

		e.player.playSound(
			e.player.location,
			Sound.ENTITY_BLAZE_DEATH,
			1f,
			0.1f
		)

		e.player.sendTitle(
			"§cSUA MONSTRA TÁ NO CHÃO!",
			"§cPegue ela antes que ela suma!",
			10,
			140,
			10
		)

		logger.info("Player ${e.player.name} dropped a Picareta Monstra at ${e.player.world.name} ${e.player.location.x}, ${e.player.location.y}, ${e.player.location.z}")
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onPlayerDropItemEvent(e: ItemDespawnEvent) {
		// Because some people are DUMB AS FUCC
		val droppedItem = e.entity.itemStack
		if (droppedItem.getStoredMetadata("isMonsterPickaxe") != "true")
			return

		val metadata = e.entity.getMetadata("owner")
			.firstOrNull()

		val playerMetadata = metadata?.value() as UUID?

		if (playerMetadata != null) {
			val player = Bukkit.getPlayer(playerMetadata)

			logger.info("Picareta Monstra despawned at ${e.location.world.name} ${e.location.x}, ${e.location.y}, ${e.location.z} by owner ${player?.name} ${metadata}")

			if (player != null) {
				player.playSound(
					player.location,
					Sound.ENTITY_BLAZE_DEATH,
					1f,
					0.01f
				)

				player.sendTitle(
					"§cSUA MONSTRA DESPAWNOU!",
					"§cQuem mandou deixar ela no chão!",
					10,
					140,
					10
				)
			}
		} else {
			logger.info("Picareta Monstra despawned at ${e.location.world.name} ${e.location.x}, ${e.location.y}, ${e.location.z} without any metadata attached")
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onPlayerDropItemEvent(e: PlayerItemBreakEvent) {
		// Because some people are DUMB AS FUCC
		val brokenItem = e.brokenItem
		if (brokenItem.getStoredMetadata("isMonsterPickaxe") != "true")
			return

		logger.info("Picareta Monstra broke at ${e.player.location.world.name} ${e.player.location.x}, ${e.player.location.y}, ${e.player.location.z}")

		e.player.playSound(
			e.player.location,
			Sound.ENTITY_BLAZE_DEATH,
			1f,
			0.05f
		)

		e.player.sendTitle(
			"§cSUA MONSTRA QUEBROU!",
			"§cQuem mandou ficar sambando com ela na mão!",
			10,
			140,
			10
		)
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onBreak(e: BlockBreakEvent) {
		val inHand = e.player.inventory.itemInMainHand

		if (e.player.inventory.itemInMainHand?.type != Material.DIAMOND_PICKAXE && e.player.inventory.itemInMainHand?.type != Material.DIAMOND_SHOVEL)
			return

		if (inHand.getStoredMetadata("isMonsterPickaxe") != "true")
			return

		val damageable = inHand.itemMeta as Damageable
		logger.info("Player ${e.player.name} used a Picareta Monstra at ${e.player.world.name} ${e.block.location.x}, ${e.block.location.y}, ${e.block.location.z}. Damage value: ${damageable.damage}")

		if (e.block.world.name == "MinaRecheada")
			return

		val claim = GriefPrevention.instance.dataStore
			.getClaimAt(e.block.location, false, null)

		if (e.block.world.name == "world" && claim == null) {
			e.player.sendMessage("§cVocê só pode usar a picareta monstra no seu terreno! Se você quer sair quebrando tudo, proteja o terreno ou vá no mundo de recursos, §6/warp recursos")
			return
		}

		val broken = e.block
		val heldItemType = e.player.inventory.itemInMainHand?.type

		if (isValidForHeldItem(heldItemType, broken)) {
			e.isCancelled = true
			val blocks = GeometryUtils.sphere(e.block.location, 2, false) as Set<Location>
			var enchantmentLevel = 0
			var efficiencyLevel = 0
			var isSilky = false
			enchantmentLevel = inHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)
			efficiencyLevel = inHand.getEnchantmentLevel(Enchantment.DURABILITY)
			isSilky = inHand.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0
			var below = false
			if (e.player.location.y > e.block.y) {
				below = true
			}

			var shouldPlay = true
			for (location in blocks) {
				if ((below || location.blockY > e.player.location.y - 1.0) && location.block.type !== Material.AIR && location.block.type !== Material.BEDROCK && isValidForHeldItem(heldItemType, location.block)) {
					val center = location.add(0.0, 0.5, 0.0)
					val claim = GriefPrevention.instance.dataStore
						.getClaimAt(location, false, null)

					if (PlayerUtils.canBreakAt(location, e.player, location.block.type) && (e.player.world.name != "world" || (e.player.world.name == "world" && claim != null))) {
						val damageable = inHand.itemMeta as Damageable
						if (chance(100.0 / (efficiencyLevel + 1))) {
							if ((damageable.damage + 1) == inHand.type.maxDurability.toInt()) {
								val name = if (heldItemType == Material.DIAMOND_PICKAXE)
									"picareta monstra"
								else
									"pá monstra"

								e.player.sendMessage("§cCuidado, a sua $name irá quebrar! Para te proteger, a gente te bloqueou de quebrar mais blocos para você poder reparar ela!")
								return
							}

							damageable.damage = damageable.damage + 1
							inHand.itemMeta = damageable as ItemMeta
						}
						if (damageable.damage > inHand.type.maxDurability) {
							e.player.inventory.removeItem(inHand)
						}

						val drops = location.block.getDrops(inHand)
							.toMutableList()
						val exp = BlockUtils.getExpCount(location.block, enchantmentLevel)

						if (!isSilky && location.block.type == Material.REDSTONE_ORE && CustomItems.checkIfRubyShouldDrop()) {
							drops.add(CustomItems.RUBY.clone())
						}

						// Using "dropItemNaturally" is kinda bad because the item can stay inside of blocks
						val dropsAsItems =  drops.map {
							location.world.dropItem(
								location,
								it
							)
						}

						doMcMMOStuff(e.player, e.block.state, dropsAsItems)

						location.block.type = Material.AIR

						if (exp > 0 && !isSilky) {
							val orb = location.block.world.spawnEntity(center, EntityType.EXPERIENCE_ORB) as ExperienceOrb
							orb.experience = exp
						}

						if (location.block.type != Material.AIR) {
							if (shouldPlay) {
								e.player.world.playSound(broken.location, Sound.ENTITY_PLAYER_LEVELUP, 0.15f, 1.25f)
								shouldPlay = false
							}
							location.world.spawnParticle(Particle.VILLAGER_HAPPY, center, 1)
							location.world.spawnParticle(Particle.FIREWORKS_SPARK, center, 1)
						} else {
							location.world.spawnParticle(Particle.SPELL_WITCH, center, 1)
						}
					} else {
						if (location.block.type === Material.AIR) {
							continue
						}
						location.world.spawnParticle(Particle.VILLAGER_ANGRY, center, 1)
					}
				}
			}
		}
	}

	fun doMcMMOStuff(player: Player, blockState: BlockState, drops: List<Item>) {
		val mcMMOPlayer: McMMOPlayer = UserManager.getPlayer(player) ?: return

		val heldItem = player.inventory.itemInMainHand

		if (com.gmail.nossr50.util.BlockUtils.affectedBySuperBreaker(blockState) && com.gmail.nossr50.util.ItemUtils.isPickaxe(heldItem) && PrimarySkillType.MINING.getPermissions(
				player
			) && !mcMMO.getPlaceStore().isTrue(blockState)
		) {
			val miningManager: MiningManager = mcMMOPlayer.miningManager
			miningManager.miningBlockCheck(blockState)

			// For my friend mcMMO xoxo
			Bukkit.getPluginManager().callEvent(
				FakeBlockDropItemEvent(
					blockState.block,
					blockState,
					player,
					drops
				)
			)
		}
	}

	class FakeBlockDropItemEvent(
		block: Block,
		blockState: BlockState,
		player:  Player,
		items: List<Item>
	) : BlockDropItemEvent(block, blockState, player, items)
}