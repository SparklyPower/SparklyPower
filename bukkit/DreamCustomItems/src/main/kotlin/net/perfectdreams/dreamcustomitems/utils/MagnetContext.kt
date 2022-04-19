package net.perfectdreams.dreamcustomitems.utils

import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.collections.mutablePlayerMapOf
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreammochilas.utils.MochilaUtils.HAS_MAGNET_KEY
import net.perfectdreams.dreammochilas.utils.MochilaUtils.IS_FULL_KEY
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.kotlin.utils.addToStdlib.flattenTo

const val magnetDurability = 9072
const val weirdMagnetDurability = 19051
val isMagnet: (ItemStack?) -> Boolean = { it?.type == Material.STONE_HOE && it.hasItemMeta() && it.itemMeta.hasCustomModelData() && it.itemMeta.customModelData in 1 .. 2 }

val magnetKey = SparklyNamespacedKey("magnet_durability")
val magnetContexts = mutablePlayerMapOf<MagnetContext>()
val magnetWhitelist = setOf(
    getAllBlocksAndOre("COPPER"), getAllBlocksAndOre("COAL"), getAllBlocksAndOre("IRON"), getAllBlocksAndOre("GOLD"),
    getAllBlocksAndOre("REDSTONE"), getAllBlocksAndOre("LAPIS"), getAllBlocksAndOre("DIAMOND"), getAllBlocksAndOre("EMERALD"),
    getRawBlockAndOre("COPPER"), getRawBlockAndOre("IRON"), getRawBlockAndOre("GOLD")
).flattenTo(mutableSetOf(Material.ANCIENT_DEBRIS, Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT, Material.QUARTZ,
    Material.NETHER_QUARTZ_ORE, Material.QUARTZ_BLOCK, Material.NETHERITE_SCRAP, Material.PRISMARINE_SHARD))

data class MagnetContext(
    val blockType: Material,
    val backpacks: List<Pair<Long, ItemStack>>,
    val customDrops: List<ItemStack>? = null,
)

val Player.hasMagnet get() =
    inventory.contents?.let {
        val backpacks = it.filter { backpack ->
            if (backpack?.type != Material.CARROT_ON_A_STICK) return@filter false
            val meta = backpack.itemMeta ?: return@filter false

            with (meta.persistentDataContainer) {
                backpack.getStoredMetadata("mochilaId") != null &&
                (has(HAS_MAGNET_KEY) && get(HAS_MAGNET_KEY, PersistentDataType.BYTE) == 1.toByte()) &&
                (has(IS_FULL_KEY) && get(IS_FULL_KEY, PersistentDataType.BYTE) == 0.toByte())
            }
        }.filterNotNull().associateBy { it.getStoredMetadata("mochilaId")!!.toLong() }.toList()

        (backpacks.isNotEmpty() || it.any(isMagnet)) to backpacks
    } ?: (false to listOf())

fun Player.isMagnetApplicable(type: Material, customDrops: List<ItemStack>? = null): Boolean =
    hasMagnet.let {
        if (it.first) {
            magnetContexts[this] = MagnetContext(type, it.second, customDrops)
            true
        } else false
    }

private fun getAllBlocksAndOre(type: String) = setOf(type, "${type}_ORE", "DEEPSLATE_${type}_ORE", "${type}_BLOCK")
    .map { Material.getMaterial(it) ?: Material.getMaterial("${it}_INGOT") ?: Material.LAPIS_LAZULI }

private fun getRawBlockAndOre(type: String) = setOf("RAW_$type", "RAW_${type}_BLOCK").map { Material.getMaterial(it)!! }