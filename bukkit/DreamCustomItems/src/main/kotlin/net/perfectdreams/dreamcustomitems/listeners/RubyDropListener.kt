package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class RubyDropListener(val m: DreamCustomItems) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBreak(e: BlockBreakEvent) {
        val inHand = e.player.inventory.itemInMainHand

        val isSilky = inHand.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0

        if (!isSilky && (e.block.type == Material.REDSTONE_ORE || e.block.type == Material.DEEPSLATE_REDSTONE_ORE) && CustomItems.checkIfRubyShouldDrop())
            e.block.world.dropItemNaturally(e.block.location, CustomItems.RUBY.clone())
    }
}