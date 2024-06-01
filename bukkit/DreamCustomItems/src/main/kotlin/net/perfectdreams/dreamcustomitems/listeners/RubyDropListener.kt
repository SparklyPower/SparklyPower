package net.perfectdreams.dreamcustomitems.listeners

import net.perfectdreams.dreamcore.utils.SparklyNamespacedBooleanKey
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcore.utils.get
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomItems.RUBY
import net.perfectdreams.dreamcustomitems.utils.CustomItems.RUBY_DROP_CHANCE
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class RubyDropListener(val m: DreamCustomItems) : Listener {
    // TODO: We should add DreamPicaretaMonstra as a dependency, but that creates a cyclic dependency...
    //  So we do this crappy hack instead, maybe we should move DreamPicaretaMonstra to here
    val IS_MONSTER_TOOL_KEY = SparklyNamespacedBooleanKey("is_monster_tool")

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onBreak(event: BlockBreakEvent) {
        with (event) {
            with (player.inventory.itemInMainHand) {
                val isMonsterTool = this.hasItemMeta() && this.itemMeta.persistentDataContainer.get(IS_MONSTER_TOOL_KEY)

                if (!isMonsterTool && this canMineRubyFrom block.type)
                    with (block) { world.dropItemNaturally(location, RUBY.clone()) }
            }
        }
    }
}

infix fun ItemStack.canMineRubyFrom(type: Material) =
    type in setOf(Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE) &&
    Enchantment.SILK_TOUCH !in enchantments.keys &&
    chance(RUBY_DROP_CHANCE)