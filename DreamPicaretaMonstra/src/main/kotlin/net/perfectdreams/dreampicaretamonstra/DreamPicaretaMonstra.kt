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
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

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
	fun onBreak(e: BlockBreakEvent) {
		val inHand = e.player.inventory.itemInMainHand

		if (e.player.inventory.itemInMainHand?.type != Material.DIAMOND_PICKAXE && e.player.inventory.itemInMainHand?.type != Material.DIAMOND_SHOVEL)
			return

		if (inHand.getStoredMetadata("isMonsterPickaxe") != "true")
			return

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
						val exp = BlockUtils.getExpCount(location.block, enchantmentLevel)

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