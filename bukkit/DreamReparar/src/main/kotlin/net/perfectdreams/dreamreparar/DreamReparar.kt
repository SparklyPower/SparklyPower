package net.perfectdreams.dreamreparar

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreampicaretamonstra.DreamPicaretaMonstra
import net.perfectdreams.dreamreparar.listeners.SignListener
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.util.*
import java.util.logging.Level

class DreamReparar : KotlinPlugin(), Listener {
	companion object {
		lateinit var INSTANCE: DreamReparar
	}

	val playerLastCheckedLocation = mutableMapOf<Player, PlayerLookingAtBlock>()

	override fun softEnable() {
		super.softEnable()

		INSTANCE = this

		registerEvents(SignListener(this))
		programarPlaca()
	}

	fun programarPlaca() {
		scheduler().schedule(this) {
			while (true) {
				var cachedHits = 0
				var notCachedHits = 0

				for (p in Bukkit.getOnlinePlayers()) {
					try {
						// Optimization: If the player didn't move, we will reuse the last checked target block, because calling getTargetBlock is very resource intensive, so it is better if we are able to skip the check
						val cachedLookingAtRepairSign = playerLastCheckedLocation[p]

						val playerLookingAtBlockWrapper = if (cachedLookingAtRepairSign?.playerLocation == p.location) {
							cachedHits++
							cachedLookingAtRepairSign
						} else {
							notCachedHits++
							val targetBlock = p.getTargetBlock(null as HashSet<Material>?, 5)

							val isRepairSign = targetBlock.type.name.contains("SIGN") && (targetBlock.state as Sign).getLine(0) == "§1[Reparar]"

							val playerLookingAtBlock = PlayerLookingAtBlock(
								p.location,
								targetBlock,
								isRepairSign
							)

							playerLastCheckedLocation[p] = PlayerLookingAtBlock(
								p.location,
								targetBlock,
								isRepairSign
							)

							playerLookingAtBlock
						}

						// Not a repair sign, bail out!
						if (!playerLookingAtBlockWrapper.isRepairSign)
							continue

						val targetBlock = playerLookingAtBlockWrapper.lookingAtBlock

						if (p.inventory.itemInMainHand.type != Material.AIR) {
							val itemReparar = p.inventory.itemInMainHand

							val meta = itemReparar.itemMeta

							if (meta is Damageable) {
								val durability = meta.damage

								if (durability != 0) {
									val lines = (targetBlock.state as Sign).lines
									lines[1] = "Grana:"
									lines[2] = "${calculatePrice(itemReparar, p)}$"
									var desconto = ""
									if (p.hasPermission("dreamreparar.vip")) {
										desconto = "10% OFF"
									}
									if (p.hasPermission("dreamreparar.vip+")) {
										desconto = "20% OFF"
									}
									if (p.hasPermission("dreamreparar.vip++")) {
										desconto = "30% OFF"
									}
									lines[3] = Math.round(calculatePercentage(itemReparar, true)).toString() + "% " + desconto
									p.sendSignChange(targetBlock.location, lines)
								} else {
									val lines = (targetBlock.state as Sign).lines
									lines[1] = "Não é"
									lines[2] = "necessário"
									lines[3] = "reparar isto!"
									p.sendSignChange(targetBlock.location, lines)
								}
							} else {
								val lines = (targetBlock.state as Sign).lines
								lines[1] = "Não é"
								lines[2] = "possível"
								lines[3] = "reparar isto!"
								p.sendSignChange(targetBlock.location, lines)
							}
						} else {
							val lines2 = (targetBlock.state as Sign).lines
							lines2[1] = "Você não pode"
							lines2[2] = "reparar a sua"
							lines2[3] = "mão!"
							p.sendSignChange(targetBlock.location, lines2)
						}
					} catch (ex: IllegalStateException) {
					} catch (ex: Exception) {
						logger.log(Level.SEVERE, "Erro ao atualizar placas para " + p.name + "!", ex)
					}
				}

				logger.info { "Updated repair sign texts! Cached hits: $cachedHits; Not cached hits: $notCachedHits" }

				waitFor(20)
			}
		}
	}

	fun calculatePercentage(itemStack: ItemStack, toMulti: Boolean): Float {
		val meta = itemStack.itemMeta

		if (meta is Damageable) {
			val durability = meta.damage

			val maxDurability = itemStack.type.maxDurability.toFloat()
			var reparar = durability / maxDurability
			if (toMulti) {
				reparar = durability / maxDurability * 100.0f
			}
			return reparar
		}

		return 0f
	}

	fun getRequiredMaterialCountFor(type: Material): Int {
		return when {
			type.name.endsWith("_PICKAXE") -> 3
			type.name.endsWith("_AXE") || type == Material.TRIDENT || type == Material.CROSSBOW -> 3
			type.name.endsWith("_SWORD") -> 2
			type.name.endsWith("_HOE") -> 2
			type.name.endsWith("_SHOVEL") -> 1
			type.name.endsWith("_HELMET") -> 5
			type.name.endsWith("_CHESTPLATE") -> 8
			type.name.endsWith("_LEGGINGS") -> 7
			type.name.endsWith("_BOOTS") -> 4
			else -> 1
		}
	}

	fun getPriceOfMaterialFor(type: Material): Double {
		return when {
			type.name.contains("NETHERITE") -> 2544.0
			type.name.contains("DIAMOND") || type == Material.ELYTRA || type == Material.TRIDENT -> 1200.0
			type.name.startsWith("CHAINMAIL_") -> 350.0
			type.name.contains("GOLDEN") -> 642.0
			type.name.contains("IRON") || type == Material.CROSSBOW -> 128.0
			type.name.contains("STONE") -> 0.4
			type.name.contains("WOODEN") -> 1.0
			else -> 128.0
		}
	}

	fun calculatePrice(itemStack: ItemStack, p: Player): Float {
		val materialCount = getRequiredMaterialCountFor(itemStack.type)
		val materialPrice = getPriceOfMaterialFor(itemStack.type)

		val totalMaterialPrice = materialCount * materialPrice

		var price = calculatePercentage(itemStack, false) * totalMaterialPrice

		for ((enchantment, value) in itemStack.enchantments) {
			if (value != 0) {
				repeat(value + 1) {
					price *= 1.05
				}
			}
		}

		if (DreamPicaretaMonstra.isMonsterTool(itemStack)) { // Picareta Monstra
			price *= 3.375f
		}

		when {
			p.hasPermission("dreamreparar.vip++") -> price * 0.7f
			p.hasPermission("dreamreparar.vip+") -> price * 0.8f
			p.hasPermission("dreamreparar.vip") -> price * 0.9f
		}

		return Math.round(price).toFloat()
	}

	fun canRepair(`is`: ItemStack): Boolean {
		return `is`.type.name.contains("SWORD") || `is`.type.name.contains("AXE") || `is`.type.name.contains("HOE") || `is`.type.name.contains("SHOVEL") || `is`.type.name.contains("HOE") || `is`.type.name.contains("PICKAXE") || `is`.type.name.contains("CHESTPLATE") || `is`.type.name.contains("BOOTS") || `is`.type.name.contains("HELMET") || `is`.type.name.contains("BARDING") || `is`.type.name.contains("LEGGINGS") || `is`.type == Material.FISHING_ROD || `is`.type == Material.BOW || `is`.type == Material.FLINT_AND_STEEL || `is`.type == Material.SHEARS || `is`.type == Material.SHIELD || `is`.type == Material.CROSSBOW || `is`.type == Material.TRIDENT
	}

	data class PlayerLookingAtBlock(
		val playerLocation: Location,
		val lookingAtBlock: Block,
		val isRepairSign: Boolean
	)
}
