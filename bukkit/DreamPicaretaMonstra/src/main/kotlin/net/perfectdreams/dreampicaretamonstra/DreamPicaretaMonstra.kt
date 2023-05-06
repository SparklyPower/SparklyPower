package net.perfectdreams.dreampicaretamonstra

import com.gmail.nossr50.datatypes.experience.XPGainReason
import com.gmail.nossr50.datatypes.player.McMMOPlayer
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.datatypes.skills.SubSkillType
import com.gmail.nossr50.mcMMO
import com.gmail.nossr50.skills.excavation.ExcavationManager
import com.gmail.nossr50.skills.mining.Mining
import com.gmail.nossr50.skills.mining.MiningManager
import com.gmail.nossr50.util.BlockUtils
import com.gmail.nossr50.util.Permissions
import com.gmail.nossr50.util.player.UserManager
import com.gmail.nossr50.util.random.RandomChanceUtil
import com.gmail.nossr50.util.skills.SkillUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreampicaretamonstra.commands.PaMonstraCommand
import net.perfectdreams.dreampicaretamonstra.commands.PicaretaMonstraCommand
import net.perfectdreams.dreampicaretamonstra.listeners.MonstraBlockListener
import net.perfectdreams.dreampicaretamonstra.listeners.MonstraDropListener
import net.perfectdreams.dreampicaretamonstra.listeners.RepairListener
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent

class DreamPicaretaMonstra : KotlinPlugin(), Listener {
	companion object {
		lateinit var INSTANCE: DreamPicaretaMonstra
		private val MINING_BLOCKS = listOf(
			Material.STONE,
			Material.NETHERRACK,
			Material.GRANITE,
			Material.DIORITE,
			Material.ANDESITE,
			Material.CALCITE,
			Material.SMOOTH_BASALT,
			Material.AMETHYST_BLOCK,
			Material.TUFF,
			Material.DEEPSLATE
		)

		private val SHOVELLING_BLOCKS = listOf(
			Material.GRASS_BLOCK,
			Material.DIRT,
			Material.SAND,
			Material.GRAVEL
		)
	}

	override fun softEnable() {
		super.softEnable()

		INSTANCE = this

		registerEvents(MonstraBlockListener(this))
		registerEvents(MonstraDropListener(this))
		registerEvents(RepairListener(this))

		registerCommand(PicaretaMonstraCommand)
		registerCommand(PaMonstraCommand)
	}

	fun isValidMiningBlock(block: Block): Boolean {
		return block.type.name.contains("ORE") || block.type in MINING_BLOCKS
	}

	fun isValidShovellingBlock(block: Block): Boolean {
		return block.type in SHOVELLING_BLOCKS
	}

	fun isValidForHeldItem(material: Material, block: Block): Boolean {
		return if (material == Material.DIAMOND_PICKAXE)
			isValidMiningBlock(block)
		else isValidShovellingBlock(block)
	}

	fun doMcMMOStuffMining(player: Player, blockState: BlockState, drops: List<Item>, heldItemHasSilkTouch: Boolean) {
		val mcMMOPlayer: McMMOPlayer = UserManager.getPlayer(player) ?: return

		val heldItem = player.inventory.itemInMainHand

		if (com.gmail.nossr50.util.BlockUtils.affectedBySuperBreaker(blockState) && com.gmail.nossr50.util.ItemUtils.isPickaxe(heldItem) && PrimarySkillType.MINING.getPermissions(
				player
			) && !mcMMO.getPlaceStore().isTrue(blockState)
		) {
			val miningManager: MiningManager = mcMMOPlayer.miningManager
			miningBlockCheck(
				player,
				mcMMOPlayer,
				PrimarySkillType.MINING,
				miningManager,
				blockState,
				heldItemHasSilkTouch
			)
		}
	}

	fun doMcMMOStuffExcavation(player: Player, blockState: BlockState, drops: List<Item>) {
		val mcMMOPlayer: McMMOPlayer = UserManager.getPlayer(player) ?: return

		val heldItem = player.inventory.itemInMainHand

		if (BlockUtils.affectedByGigaDrillBreaker(blockState) && com.gmail.nossr50.util.ItemUtils.isShovel(heldItem) && PrimarySkillType.EXCAVATION.getPermissions(
				player
			) && !mcMMO.getPlaceStore().isTrue(blockState)
		) {
			val excavationManager: ExcavationManager = mcMMOPlayer.excavationManager
			excavationManager.excavationBlockCheck(blockState)
		}
	}

	// This is the same thing as mcMMO's "miningBlockCheck" in MiningManager, however we have a "heldItemHasSilkTouch" boolean to avoid checking the "containsEnchantment", which is very intensive
	private fun miningBlockCheck(
		player: Player,
		mmoPlayer: McMMOPlayer,
		skillType: PrimarySkillType,
		miningManager: MiningManager,
		blockState: BlockState,
		heldItemHasSilkTouch: Boolean
	) {
		miningManager.applyXpGain(Mining.getBlockXp(blockState).toFloat(), XPGainReason.PVE)
		if (Permissions.isSubSkillEnabled(player, SubSkillType.MINING_DOUBLE_DROPS)) {
			if (mmoPlayer.getAbilityMode(mcMMO.p.skillTools.getSuperAbility(skillType))) {
				SkillUtils.handleDurabilityChange(
					player.inventory.itemInMainHand,
					mcMMO.p.generalConfig.abilityToolDamage
				)
			}
			if (mcMMO.p.generalConfig.getDoubleDropsEnabled(
					PrimarySkillType.MINING,
					blockState.type
				) && miningManager.canDoubleDrop()
			) {
				if (!heldItemHasSilkTouch || mcMMO.p.advancedConfig.doubleDropSilkTouchEnabled) {
					if (RandomChanceUtil.checkRandomChanceExecutionSuccess(
							player,
							SubSkillType.MINING_DOUBLE_DROPS,
							true
						)
					) {
						val useTriple = mmoPlayer.getAbilityMode(mcMMO.p.skillTools.getSuperAbility(skillType)) && mcMMO.p.advancedConfig.allowMiningTripleDrops
						BlockUtils.markDropsAsBonus(blockState, useTriple)
					}
				}
			}
		}
	}

	class FakeBlockDropItemEvent(
		block: Block,
		blockState: BlockState,
		player:  Player,
		items: List<Item>
	) : BlockDropItemEvent(block, blockState, player, items)
}
