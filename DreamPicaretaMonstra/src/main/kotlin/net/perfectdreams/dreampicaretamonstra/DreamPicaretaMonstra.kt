package net.perfectdreams.dreampicaretamonstra

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.storeMetadata
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
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
	fun onBreak(e: BlockBreakEvent) {
		val inHand = e.player.inventory.itemInMainHand

		if (e.player.inventory.itemInMainHand?.type != Material.DIAMOND_PICKAXE && e.player.inventory.itemInMainHand?.type != Material.DIAMOND_SHOVEL)
			return

		if (inHand.getStoredMetadata("isMonsterPickaxe") != "true")
			return

		if (e.block.world.name == "MinaRecheada")
			return

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
					if (PlayerUtils.canBreakAt(location, e.player, location.block.type)) {
						val count = BlockUtils.getDropCount(enchantmentLevel, location.block)
						val exp = BlockUtils.getExpCount(location.block, enchantmentLevel)
						val mat = location.block.type // Bloco original
						var material = BlockUtils.getDropType(mat) // Novo tipo

						if (mat == Material.STONE)
							material = Material.COBBLESTONE
						if (mat == Material.GRASS_BLOCK)
							material = Material.DIRT

						location.block.type = Material.AIR

						location.world.dropItemNaturally(location, ItemStack(if (isSilky) mat else material, if (isSilky) 1 else count))

						if (exp > 0 && !isSilky) {
							val orb = location.block.world.spawnEntity(center, EntityType.EXPERIENCE_ORB) as ExperienceOrb
							orb.experience = exp
						}

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

						if (mat.name.contains("ORE")) {
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
}