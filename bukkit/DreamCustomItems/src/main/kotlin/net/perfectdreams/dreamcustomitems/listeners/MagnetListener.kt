package net.perfectdreams.dreamcustomitems.listeners

import com.gmail.nossr50.datatypes.meta.BonusDropMeta
import com.gmail.nossr50.events.skills.repair.McMMOPlayerRepairCheckEvent
import com.gmail.nossr50.events.skills.salvage.McMMOPlayerSalvageCheckEvent
import com.gmail.nossr50.util.MetadataConstants
import net.kyori.adventure.text.Component
import net.perfectdreams.dreamcore.utils.canHoldItem
import net.perfectdreams.dreamcore.utils.extensions.formatted
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.extensions.playSoundAndSendMessage
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.listeners.canMineRubyFrom
import net.perfectdreams.dreamcustomitems.utils.CustomItems.IS_MICROWAVE_KEY
import net.perfectdreams.dreamcustomitems.utils.CustomItems.IS_SUPERFURNACE_KEY
import net.perfectdreams.dreamcustomitems.utils.CustomItems.IS_TRASHCAN_KEY
import net.perfectdreams.dreamcustomitems.utils.*
import net.perfectdreams.dreammochilas.utils.MochilaInventoryHolder
import net.perfectdreams.dreammochilas.utils.MochilaUtils
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.Skull
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import kotlin.math.ceil

private typealias Drops = MutableList<ItemStack>
private typealias Blacklist = Set<Material?>
private typealias Backpack = Pair<Long, ItemStack>

class MagnetListener(val m: DreamCustomItems) : Listener {
    private val crops = setOf(Material.WHEAT, Material.NETHER_WART, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, Material.PUMPKIN, Material.MELON)
    private val axes = setOf(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE)
    private val logs =  setOf(Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG)

    @EventHandler(priority = EventPriority.HIGH)
    fun onDropitem(event: BlockDropItemEvent) {
        with (event) {
            if (player in magnetContexts) {
                val blacklist = m.dropsBlacklist[player]?.contents?.mapTo(mutableSetOf()) { it?.type } ?: setOf()

                val mcMMOBonus = block.getMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS).getOrNull(0)?.let {
                    block.removeMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS, m.mcMMO)
                    (it as BonusDropMeta).asInt()
                } ?: 0

                with (magnetContexts.remove(player)!!) {
                    val drops = (items.map { it.itemStack } + (customDrops ?: listOf())).toMutableList()

                    with (drops) {
                        firstOrNull()?.let { it.amount += mcMMOBonus }
                        if (player.inventory.itemInMainHand canMineRubyFrom blockType) add(CustomItems.RUBY.clone())
                    }

                    player.inventory.addDrops(drops, blacklist, player)

                    if (backpacks.isNotEmpty()) addDropsToBackpack(drops, blacklist, player, backpacks.iterator(), block)
                    else drops.forEach { with (block) { if (it.type != Material.AIR) world.dropItemNaturally(location, it) } }

                    if (items.isNotEmpty()) items.clear()
                }
            }
        }
    }

    private fun addDropsToBackpack(drops: Drops, blacklist: Blacklist, player: Player, iterator: Iterator<Backpack>, block: Block) {
        with (iterator.next()) {
            m.launchAsyncThread {
                val triggerType = "Trying to attract items to ${player.name}'s backpack"
                MochilaUtils.retrieveMochilaAndHold(first, triggerType)?.let {
                    val inventory = it.getOrCreateMochilaInventoryAndHold()

                    onMainThread {
                        inventory.addDrops(drops, blacklist, player)
                        MochilaUtils.updateMochilaItemLore(inventory, second)

                        if (drops.isNotEmpty() && iterator.hasNext()) addDropsToBackpack(drops, blacklist, player, iterator, block)
                        else drops.forEach { with (block) { if (it.type != Material.AIR) world.dropItemNaturally(location, it) } }
                    }

                    /**
                     * This issue only happens when the player is mining TOO FAST (e.g. using mcMMO super breaker skill)
                     * To circumvent it, we release every hold that is still active by the time the async thread gets to it
                     */
                    try {
                        (inventory.holder as MochilaInventoryHolder).accessHolders.forEach { holder ->
                            holder?.wrapper?.let { wrapper ->
                                if (wrapper.holds > 0)
                                    wrapper.release("Successfully attracted applicable items to ${player.name}'s backpack")
                            }
                        }
                    } catch (exception: ConcurrentModificationException) {
                        m.logger.warning { "${player.name} is mining too fast, but there shouldn't be any issues saving the backpack to the database." }
                    }
                }
            }
        }
    }

    private fun Inventory.addDrops(drops: Drops, blacklist: Blacklist, player: Player) =
        firstOrNull(isMagnet)?.let { magnet ->
            val forRemoval = mutableListOf<ItemStack>()
            val isNormalMagnet = magnet.itemMeta.customModelData == 1
            val maxDurability = if (isNormalMagnet) magnetDurability else weirdMagnetDurability
            var currentDurability = magnet.itemMeta.persistentDataContainer.get(magnetKey, PersistentDataType.INTEGER) ?: maxDurability

            if (currentDurability <= 0) return@let

            drops.forEach {
                val canPickup = (it.type !in blacklist && if (isNormalMagnet) it.type in magnetWhitelist else true) && currentDurability > 0

                if (canHoldItem(it) && canPickup) {
                    addItem(it)
                    forRemoval.add(it)
                    currentDurability -= it.amount
                } else if (!canHoldItem(it) && canPickup)
                    player.sendActionBar(Component.text("§c§lO ímã não pôde puxar alguns itens pois não há espaço suficiente."))
            }

            magnet.meta<Damageable> {
                damage = ceil(131 - currentDurability.toFloat() / maxDurability * 131).toInt()
                persistentDataContainer.set(magnetKey, PersistentDataType.INTEGER, currentDurability.let { if (it < 0) 0 else it })
                lore = lore!!.apply { set(lastIndex, "§6Usos restantes: §f${currentDurability.formatted} §6/ §f${maxDurability.formatted}") }
            }

            if (currentDurability < 0) player.playSoundAndSendMessage(Sound.ENTITY_ITEM_BREAK, "§cUm dos seus ímãs descarregou.")

            drops.removeAll(forRemoval)
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