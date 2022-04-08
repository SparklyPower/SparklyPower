package net.perfectdreams.dreamxizum.gui.pages.items.enchantments

import net.perfectdreams.dreamcore.utils.extensions.removeAllEnchantments
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

class EnchantCrossbowPage(val player: Player) : AbstractPage() {
    private val options = player.battle!!.options
    private val enchantments = listOf(
        EnchantmentAndLevel(Enchantment.PIERCING, 4),
        EnchantmentAndLevel(Enchantment.QUICK_CHARGE, 3),
        EnchantmentAndLevel(Enchantment.MULTISHOT, 1)
    )

    init {
        onReturn()

        with(npcs) {
            listOf(crossbowPiercing, crossbowQuickCharge, crossbowMultishot).forEachIndexed { index, model ->
                val ench = enchantments[index]
                enchantableButton(model, ench.maxLevel) { _, level ->
                    if (level > 0) options.items.first { it.type == Material.CROSSBOW }.addEnchantment(ench.enchantment, level)
                    else options.items.first { it.type == Material.CROSSBOW }.removeEnchantment(ench.enchantment)
                }.npc.changeItem(Material.CROSSBOW.toItemStack())
            }
        }
    }

    override fun onBack() { options.items.first { it.type == Material.CROSSBOW }.apply { removeAllEnchantments() } }
    override fun onReturn() { nextPage = Paginator.inferNextPage(player, 2) }
}