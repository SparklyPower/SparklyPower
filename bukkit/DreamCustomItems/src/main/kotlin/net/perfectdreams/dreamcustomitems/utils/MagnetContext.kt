package net.perfectdreams.dreamcustomitems.utils

import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.jetbrains.kotlin.utils.addToStdlib.flattenTo

val isMagnet: (ItemStack?) -> Boolean = { it?.type == Material.STONE_HOE && it.itemMeta?.customModelData in 1 .. 2 }
val magnetKey = SparklyNamespacedKey("magnet_durability")
val magnetContexts = mutablePlayerMapOf<MagnetContext>()
val magnetWhitelist = setOf(
    getAllBlocksAndOre("COPPER"), getAllBlocksAndOre("COAL"), getAllBlocksAndOre("IRON"), getAllBlocksAndOre("GOLD"),
    getAllBlocksAndOre("REDSTONE"), getAllBlocksAndOre("LAPIS"), getAllBlocksAndOre("DIAMOND"), getAllBlocksAndOre("EMERALD"),
    getRawBlockAndOre("COPPER"), getRawBlockAndOre("IRON"), getRawBlockAndOre("GOLD")
).flattenTo(mutableSetOf(Material.ANCIENT_DEBRIS, Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT, Material.QUARTZ,
    Material.NETHER_QUARTZ_ORE, Material.QUARTZ_BLOCK, Material.NETHERITE_SCRAP, Material.PRISMARINE_SHARD))

data class MagnetContext(
    val magnet: ItemStack,
    val blockType: Material,
    val inventory: PlayerInventory,
    val customDrops: List<ItemStack>? = null
)

fun Player.isMagnetApplicable(type: Material, customDrops: List<ItemStack>? = null): Boolean =
    inventory.contents?.firstOrNull(isMagnet)?.let {
        magnetContexts[this] = MagnetContext(it, type, inventory, customDrops)
        true
    } ?: false

private fun getAllBlocksAndOre(type: String) = setOf(type, "${type}_ORE", "DEEPSLATE_${type}_ORE", "${type}_BLOCK")
    .map { Material.getMaterial(it) ?: Material.getMaterial("${it}_INGOT") ?: Material.LAPIS_LAZULI }

private fun getRawBlockAndOre(type: String) = setOf("RAW_$type", "RAW_${type}_BLOCK").map { Material.getMaterial(it)!! }