package net.perfectdreams.dreamcustomitems.listeners

import com.gmail.nossr50.datatypes.meta.BonusDropMeta
import com.gmail.nossr50.events.skills.repair.McMMOPlayerRepairCheckEvent
import com.gmail.nossr50.events.skills.salvage.McMMOPlayerSalvageCheckEvent
import com.gmail.nossr50.util.MetadataConstants
import net.kyori.adventure.text.Component
import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.playSoundAndSendMessage
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.listeners.RubyDropListener.Companion.redstoneOres
import net.perfectdreams.dreamcustomitems.utils.CustomItems.IS_MICROWAVE_KEY
import net.perfectdreams.dreamcustomitems.utils.CustomItems.IS_SUPERFURNACE_KEY
import net.perfectdreams.dreamcustomitems.utils.CustomItems.IS_TRASHCAN_KEY
import net.perfectdreams.dreamcustomitems.utils.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.ceil

class MagnetListener(val m: DreamCustomItems) : Listener {
    private val crops = setOf(Material.WHEAT, Material.NETHER_WART, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.PUMPKIN, Material.MELON)
    private val axes = setOf(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE)
    private val logs =  setOf(Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG)
    private val formatter = NumberFormat.getNumberInstance(Locale.GERMAN) as DecimalFormat

    @EventHandler(priority = EventPriority.HIGH)
    fun onDropitem(event: BlockDropItemEvent) {
        with (event) {
            if (player in magnetContexts) {
                val mcMMOBonus = block.getMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS).getOrNull(0)?.let {
                    block.removeMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS, m.mcMMO)
                    (it as BonusDropMeta).asInt()
                } ?: 0

                val blacklist = m.dropsBlacklist[player]?.contents?.mapTo(mutableSetOf()) { it?.type } ?: setOf()

                with (magnetContexts.remove(player)!!) {
                    var drops = (items.map { it.itemStack } + (customDrops ?: listOf())).toMutableList()

                    with (drops) {
                        firstOrNull()?.let { it.amount += mcMMOBonus }

                        if (blockType in redstoneOres
                            && CustomItems.checkIfRubyShouldDrop()
                            && Enchantment.SILK_TOUCH !in inventory.itemInMainHand.enchantments.keys
                        ) add(CustomItems.RUBY.clone())
                    }

                    val isNormalMagnet = magnet.itemMeta.customModelData == 1
                    val maxDurability = if (isNormalMagnet) 8640 else 18144
                    var currentDurability = magnet.itemMeta.persistentDataContainer.get(magnetKey, PersistentDataType.INTEGER) ?: maxDurability
                    var attractedItems = 0

                    drops.forEach {
                        val canPickup = it.type !in blacklist && if (isNormalMagnet) it.type in magnetWhitelist else true

                        if (inventory.canHoldItem(it) && canPickup) {
                            inventory.addItem(it)
                            attractedItems += it.amount
                        } else {
                            if (!inventory.canHoldItem(it) && canPickup)
                                player.sendActionBar(Component.text("§c§lO ímã não pôde puxar o item pois seu inventário está cheio."))
                            with (block) { world.dropItemNaturally(location, it) }
                        }
                    }

                    currentDurability -= attractedItems

                    if (currentDurability <= 0) {
                        inventory.removeItem(magnet)
                        player.playSoundAndSendMessage(Sound.ENTITY_ITEM_BREAK, "§cSeu ímã quebrou.")
                    } else {
                        magnet.meta<Damageable> { damage = ceil(131 - currentDurability.toFloat() / maxDurability * 131).toInt() }
                        magnet.meta<ItemMeta> {
                            persistentDataContainer.set(magnetKey, PersistentDataType.INTEGER, currentDurability)
                            lore = lore!!.apply { set(lastIndex, "§6Usos restantes: §f${formatter.format(currentDurability)} / ${formatter.format(maxDurability)}") }
                        }
                    }

                    if (items.isNotEmpty()) items.clear()
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBreak(event: BlockBreakEvent) {
        with (event) {
            if (block.isCustomHead) return
            if (block.type in crops) return
            with (player.inventory.itemInMainHand) {
                if (getStoredMetadata("isMoveSpawner") == "true") return
                if (getStoredMetadata("isMonsterPickaxe") == "true") return
                if (!player.isSneaking && type in axes && block.type in logs) return
                player.isMagnetApplicable(block.type)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDamage(event: PlayerItemDamageEvent) { with (event) { isCancelled = isMagnet.invoke(item) } }
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAnvilRepair(event: PrepareAnvilEvent) { with (event.inventory) { if (any(isMagnet)) repairCost = Int.MAX_VALUE } }
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onMcMMORepair(event: McMMOPlayerRepairCheckEvent) { with (event) { isCancelled = isMagnet.invoke(repairedObject) } }
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onMcMMOSalvage(event: McMMOPlayerSalvageCheckEvent) { with (event) { isCancelled = isMagnet.invoke(salvageItem) } }
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEnchant(event: EnchantItemEvent) { with (event) { isCancelled = isMagnet.invoke(item) } }

    private val Block.isCustomHead get() =
        if (type in setOf(Material.PLAYER_HEAD, Material.PLAYER_WALL_HEAD))
            setOf(IS_MICROWAVE_KEY, IS_SUPERFURNACE_KEY, IS_TRASHCAN_KEY).any {
                (state as Skull).persistentDataContainer.has(it)
            }
        else false
}