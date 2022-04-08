package net.perfectdreams.dreamxizum.gui.pages.items

import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import net.perfectdreams.dreamxizum.gui.pages.items.enchantments.EnchantArmorPage
import net.perfectdreams.dreamxizum.gui.pages.kits.PluginKitsPage.Companion.getArmor
import org.bukkit.inventory.ItemStack

class ArmorPage(val options: BattleOptions) : AbstractPage() {
    companion object {
        val armors = listOf(getArmor("IRON"), getArmor("DIAMOND"), getArmor("NETHERITE"))
            .map { it.toMutableSet() }
    }

    private val models = with (npcs) { listOf(ironArmor, diamondArmor, netheriteArmor) }

    init {
        models.forEachIndexed { index, model ->
            button(model) {
                options.armor = mutableSetOf<ItemStack>().apply { addAll(armors[index]) }
                Paginator.fetch(it).addAndShowPage(EnchantArmorPage(armors[index], it))
            }.npc.equipment = mutableSetOf<ItemStack>().apply { addAll(armors[index]) }
        }
    }
}