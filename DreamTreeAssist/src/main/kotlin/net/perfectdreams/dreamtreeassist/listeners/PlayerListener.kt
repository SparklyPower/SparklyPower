package net.perfectdreams.dreamtreeassist.listeners

import com.gmail.nossr50.datatypes.player.McMMOPlayer
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.mcMMO
import com.gmail.nossr50.skills.woodcutting.WoodcuttingManager
import com.gmail.nossr50.util.BlockUtils
import com.gmail.nossr50.util.player.UserManager
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.extensions.canBreakAt
import net.perfectdreams.dreamtreeassist.DreamTreeAssist
import net.perfectdreams.dreamtreeassist.utils.BlockLocation
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.data.type.Leaves
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

class PlayerListener(val m: DreamTreeAssist) : Listener {
    private val logs = setOf(
        Material.ACACIA_LOG,
        Material.BIRCH_LOG,
        Material.DARK_OAK_LOG,
        Material.JUNGLE_LOG,
        Material.OAK_LOG,
        Material.SPRUCE_LOG
    )

    private val leaves = setOf(
        Material.ACACIA_LEAVES,
        Material.BIRCH_LEAVES,
        Material.DARK_OAK_LEAVES,
        Material.JUNGLE_LEAVES,
        Material.OAK_LEAVES,
        Material.SPRUCE_LEAVES
    )

    private val axes = setOf(
        Material.WOODEN_AXE,
        Material.STONE_AXE,
        Material.IRON_AXE,
        Material.GOLDEN_AXE,
        Material.DIAMOND_AXE
    )

    private val logToSapling = mapOf(
        Material.ACACIA_LOG to Material.ACACIA_SAPLING,
        Material.BIRCH_LOG to Material.BIRCH_SAPLING,
        Material.DARK_OAK_LOG to Material.DARK_OAK_SAPLING,
        Material.JUNGLE_LOG to Material.JUNGLE_SAPLING,
        Material.OAK_LOG to Material.OAK_SAPLING,
        Material.SPRUCE_LOG to Material.SPRUCE_SAPLING
    )

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlace(e: BlockPlaceEvent) {
        if (e.block.type !in logs)
            return

        if (e.block.type in logs) {
            m.placedLogs.add(
                BlockLocation(
                    e.block.world.name,
                    e.block.x,
                    e.block.y,
                    e.block.z
                )
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: BlockBreakEvent) {
        if (e.block.type in logs) {
            // Remove log from the placed logs cache
            m.placedLogs.remove(
                BlockLocation(
                    e.block.world.name,
                    e.block.x,
                    e.block.y,
                    e.block.z
                )
            )
        }

        if (e.player.isSneaking)
            return

        val clickedBlock = e.block

        if (clickedBlock.type !in logs)
            return

        val heldItem = e.player.inventory.itemInMainHand

        if (heldItem.type !in axes)
            return

        val position = BlockLocation(
            clickedBlock.world.name,
            clickedBlock.x,
            clickedBlock.y,
            clickedBlock.z
        )

        if (m.placedLogs.contains(position)) // If it is a player placed block, break it normally
            return

        e.isCancelled = true

        processTree(e.player, heldItem, e.block)
    }

    private fun processTree(player: Player, heldItem: ItemStack, block: Block) {
        val blocksToBeDestroyed = mutableListOf<Block>()

        getAllBlocksFromTree(player, block, block, blocksToBeDestroyed)

        val lowestLog = blocksToBeDestroyed.asSequence().filter { it.type in logs }.minBy { it.y }
        val lowestLogType = lowestLog?.type

        for ((index, blockToBeDestroyed) in blocksToBeDestroyed.withIndex()) {
            val drops = blockToBeDestroyed.getDrops(heldItem)

            drops.forEach {
                blockToBeDestroyed.world.dropItemNaturally(blockToBeDestroyed.location, it)
            }

            if (blockToBeDestroyed.type in logs) {
                // Only damage if it is a log, damaging due to leaves is kinda meh
                val efficiencyLevel = heldItem.getEnchantmentLevel(Enchantment.DURABILITY)

                if (chance(100.0 / (efficiencyLevel + 1))) {
                    val damageable = heldItem.itemMeta as Damageable
                    damageable.damage += 1
                    heldItem.itemMeta = damageable as ItemMeta

                    if (damageable.damage > heldItem.type.maxDurability) {
                        player.inventory.removeItem(heldItem)
                        return
                    }
                }

                if (index % 4 == 0) // Do not give *that* much XP
                    doMcMMOStuff(player, blockToBeDestroyed.state)
            }

            blockToBeDestroyed.type = Material.AIR
        }

        if (lowestLog != null && lowestLogType != null && lowestLog.type == Material.AIR && (lowestLog.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK || lowestLog.getRelative(BlockFace.DOWN).type == Material.DIRT)) {
            val saplingType = logToSapling[lowestLogType]

            if (saplingType != null)
                lowestLog.type = saplingType
        }
    }

    private fun getAllBlocksFromTree(player: Player, initialBlock: Block, block: Block, list: MutableList<Block>): List<Block> {
        if (block in list)
            return list

        if (block.type !in logs && block.type !in leaves)
            return list

        if (block.type in leaves) {
            val leaves = block.blockData as Leaves
            if (leaves.isPersistent) // Player placed leaves
                return list
        }

        if (!player.canBreakAt(block.location, block.type))
            return list

        val position = BlockLocation(
            block.world.name,
            block.x,
            block.y,
            block.z
        )

        if (m.placedLogs.contains(position))
            return list

        val distance = initialBlock.location.distanceSquared(block.location.clone().apply { y = initialBlock.y.toDouble() })

        if (distance > 36) // 6 blocks, this is to avoid chopping waaaay too much trees in tree heavy biomes
            return list

        list.add(block)

        getAllBlocksFromTree(player, initialBlock, block.getRelative(BlockFace.UP), list)
        getAllBlocksFromTree(player, initialBlock, block.getRelative(BlockFace.DOWN), list)
        getAllBlocksFromTree(player, initialBlock, block.getRelative(BlockFace.NORTH), list)
        getAllBlocksFromTree(player, initialBlock, block.getRelative(BlockFace.SOUTH), list)
        getAllBlocksFromTree(player, initialBlock, block.getRelative(BlockFace.EAST), list)
        getAllBlocksFromTree(player, initialBlock, block.getRelative(BlockFace.WEST), list)

        return list
    }

    private fun doMcMMOStuff(player: Player, blockState: BlockState) {
        val mcMMOPlayer: McMMOPlayer = UserManager.getPlayer(player) ?: return

        if (BlockUtils.isLog(blockState) && PrimarySkillType.WOODCUTTING.getPermissions(player) && !mcMMO.getPlaceStore().isTrue(blockState)) {
            val miningManager: WoodcuttingManager = mcMMOPlayer.woodcuttingManager
            miningManager.woodcuttingBlockCheck(blockState)
        }
    }
}