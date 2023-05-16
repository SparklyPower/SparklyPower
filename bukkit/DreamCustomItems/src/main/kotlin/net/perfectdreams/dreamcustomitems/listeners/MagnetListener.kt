package net.perfectdreams.dreamcustomitems.listeners

import com.gmail.nossr50.events.items.McMMOItemSpawnEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.get
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.MagnetUtils
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.Damageable

class MagnetListener(val m: DreamCustomItems) : Listener {
    private val magnetizableBlocks = Material.values()
        .filter { it.name.endsWith("_ORE") }
        .toSet()

    private val weirdMagnetizableBlocks = magnetizableBlocks + setOf(
        Material.STONE,
        Material.DIRT,
        Material.GRASS_BLOCK,
        Material.GRAVEL,
        Material.COBBLESTONE,
        Material.GRANITE,
        Material.DIORITE,
        Material.ANDESITE,
        Material.DEEPSLATE,
        Material.COBBLED_DEEPSLATE,
        Material.CALCITE,
        Material.SMOOTH_BASALT,
        Material.TUFF,
        Material.DRIPSTONE_BLOCK,
        Material.NETHERRACK,
        Material.BASALT,
        Material.BLACKSTONE,

        Material.COAL,
        Material.COAL_BLOCK,

        Material.RAW_IRON,
        Material.RAW_IRON_BLOCK,
        Material.IRON_BLOCK,

        Material.RAW_COPPER,
        Material.RAW_COPPER_BLOCK,
        Material.COPPER_BLOCK,
        Material.EXPOSED_COPPER,
        Material.WEATHERED_COPPER,
        Material.OXIDIZED_COPPER,
        Material.WAXED_COPPER_BLOCK,
        Material.WAXED_EXPOSED_COPPER,
        Material.WAXED_WEATHERED_COPPER,
        Material.WAXED_OXIDIZED_COPPER,

        Material.RAW_GOLD,
        Material.RAW_GOLD_BLOCK,
        Material.GOLD_NUGGET,
        Material.GOLD_BLOCK,

        Material.REDSTONE,
        Material.REDSTONE_BLOCK,

        Material.EMERALD,
        Material.EMERALD_BLOCK,

        Material.LAPIS_LAZULI,
        Material.LAPIS_BLOCK,

        Material.DIAMOND,
        Material.DIAMOND_BLOCK,

        Material.QUARTZ,
        Material.QUARTZ_BLOCK,

        Material.ANCIENT_DEBRIS,
        Material.NETHERITE_BLOCK,

        Material.AMETHYST_SHARD,
        Material.BUDDING_AMETHYST,
        Material.AMETHYST_BLOCK,
        Material.SMALL_AMETHYST_BUD,
        Material.MEDIUM_AMETHYST_BUD,
        Material.LARGE_AMETHYST_BUD,
        Material.AMETHYST_CLUSTER,

        Material.FLINT,
    )

    @EventHandler(ignoreCancelled = false)
    fun onMagnetInteract(e: PlayerInteractEvent) {
        val item = e.item ?: return

        val magnet = MagnetUtils.getMagnetType(item) ?: return

        e.isCancelled = true

        val currentDurability = item.itemMeta.persistentDataContainer.get(MagnetUtils.MAGNET_DURABILITY) ?: 0

        e.player.sendMessage(
            textComponent {
                color(NamedTextColor.GREEN)

                append("Usos do Ímã: ") {}
                append(currentDurability.toString()) {
                    color(NamedTextColor.YELLOW)
                }
                append("/")
                append(magnet.maxDamage.toString()) {
                    color(NamedTextColor.YELLOW)
                }
            }
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockDropItem(e: BlockDropItemEvent) {
        // Are we holding a magnet?
        val magnetsInPlayerInventory = e.player.inventory.mapNotNull {
            if (it == null)
                return@mapNotNull null

            val magnetType = MagnetUtils.getMagnetType(it) ?: return@mapNotNull null

            MagnetUtils.Magnet(
                it,
                magnetType
            )
        }

        val magnet = magnetsInPlayerInventory.firstOrNull() ?: return

        when (magnet.type) {
            MagnetUtils.MagnetType.MAGNET -> {
                if (e.blockState.type !in magnetizableBlocks)
                    return
            }
            MagnetUtils.MagnetType.WEIRD_MAGNET -> {
                if (e.blockState.type !in weirdMagnetizableBlocks)
                    return
            }
        }

        val items = e.items.toList()

        for (item in items) {
            // Ignore items that are in our disallowed drops list
            val playerDisallowedDrops = m.dropsBlacklist[e.player]?.mapNotNull { it?.type }
            if (playerDisallowedDrops != null && item.itemStack.type in playerDisallowedDrops)
                continue

            // If we can hold the item in our inventory, we will add it to our inventory and remove it from the drops list
            if (e.player.inventory.canHoldItem(item.itemStack)) {
                val meta = magnet.itemStack.itemMeta as Damageable
                val currentDurability = meta.persistentDataContainer.get(MagnetUtils.MAGNET_DURABILITY) ?: 0
                if (currentDurability >= magnet.type.maxDamage) // You need to repair your magnet at this point
                    return

                val newDurability = currentDurability + 1

                meta.persistentDataContainer.set(MagnetUtils.MAGNET_DURABILITY, newDurability)
                val mapMagnetDurabilityToVanillaToolDurabilityMultiplier = magnet.itemStack.type.maxDurability.toDouble() / magnet.type.maxDamage.toDouble()

                meta.damage = (currentDurability * mapMagnetDurabilityToVanillaToolDurabilityMultiplier).toInt()
                magnet.itemStack.itemMeta = meta

                e.items.remove(item)
                e.player.inventory.addItem(item.itemStack)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onMcMMOBlockDropItem(e: McMMOItemSpawnEvent) {
        // Are we holding a magnet?
        val player = e.player ?: return

        val magnetsInPlayerInventory = player.inventory.mapNotNull {
            if (it == null)
                return@mapNotNull null

            val magnetType = MagnetUtils.getMagnetType(it) ?: return@mapNotNull null

            MagnetUtils.Magnet(
                it,
                magnetType
            )
        }

        val magnet = magnetsInPlayerInventory.firstOrNull() ?: return

        // We can't get the magnet type, so we will default to the weird magnetizable blocks
        val itemStack = e.itemStack

        // Ignore items that are in our disallowed drops list
        val playerDisallowedDrops = m.dropsBlacklist[e.player]?.mapNotNull { it?.type }
        if (playerDisallowedDrops != null && itemStack.type in playerDisallowedDrops)
            return

        // If we can hold the item in our inventory, we will add it to our inventory and remove it from the drops list
        if (player.inventory.canHoldItem(itemStack)) {
            val meta = magnet.itemStack.itemMeta as Damageable
            val currentDurability = meta.persistentDataContainer.get(MagnetUtils.MAGNET_DURABILITY) ?: 0
            if (currentDurability >= magnet.type.maxDamage) // You need to repair your magnet at this point
                return

            val newDurability = currentDurability + 1

            meta.persistentDataContainer.set(MagnetUtils.MAGNET_DURABILITY, newDurability)
            val mapMagnetDurabilityToVanillaToolDurabilityMultiplier = magnet.itemStack.type.maxDurability.toDouble() / magnet.type.maxDamage.toDouble()

            meta.damage = (currentDurability * mapMagnetDurabilityToVanillaToolDurabilityMultiplier).toInt()
            magnet.itemStack.itemMeta = meta

            e.isCancelled = true
            player.inventory.addItem(itemStack)
        }
    }
}