package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomItems.checkIfRubyShouldDrop
import net.perfectdreams.dreamcustomitems.utils.CustomItems.RUBY
import net.perfectdreams.dreamcustomitems.utils.magnetContexts
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class RubyDropListener(val m: DreamCustomItems) : Listener {
    companion object { val redstoneOres = setOf(Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE) }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        with (event) {
            if (player !in magnetContexts) {
                with (block) {
                    if (type !in redstoneOres) return
                    if (!checkIfRubyShouldDrop()) return
                    if (Enchantment.SILK_TOUCH in player.inventory.itemInMainHand.enchantments.keys) return
                    world.dropItemNaturally(location, RUBY.clone())
                }
            }
        }
    }
}