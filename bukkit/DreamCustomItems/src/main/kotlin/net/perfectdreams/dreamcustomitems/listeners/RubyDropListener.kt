package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomItems.RUBY
import net.perfectdreams.dreamcustomitems.utils.CustomItems.RUBY_DROP_CHANCE
import net.perfectdreams.dreamcustomitems.utils.magnetContexts
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class RubyDropListener(val m: DreamCustomItems) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBreak(event: BlockBreakEvent) {
        with (event) {
            with (player.inventory.itemInMainHand) {
                if (player !in magnetContexts && getStoredMetadata("isMonsterPickaxe") != "true" && this canMineRubyFrom block.type)
                    with (block) { world.dropItemNaturally(location, RUBY.clone()) }
            }
        }
    }
}

infix fun ItemStack.canMineRubyFrom(type: Material) =
    type in setOf(Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE) &&
    Enchantment.SILK_TOUCH !in enchantments.keys &&
    chance(RUBY_DROP_CHANCE)