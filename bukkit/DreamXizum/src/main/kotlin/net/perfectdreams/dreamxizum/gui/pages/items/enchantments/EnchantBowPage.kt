package net.perfectdreams.dreamxizum.gui.pages.items.enchantments

import net.perfectdreams.dreamcore.utils.extensions.removeAllEnchantments
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

class EnchantBowPage(val player: Player) : AbstractPage() {
    private val options = player.battle!!.options
    private val enchantments = listOf(
        EnchantmentAndLevel(Enchantment.ARROW_DAMAGE, 5),
        EnchantmentAndLevel(Enchantment.ARROW_KNOCKBACK, 2),
        EnchantmentAndLevel(Enchantment.ARROW_FIRE, 1),
        EnchantmentAndLevel(Enchantment.ARROW_INFINITE, 1)
    )

    init {
        onReturn()

        with(npcs) {
            listOf(bowPower, bowPunch, bowFlame, bowInfinity).forEachIndexed { index, model ->
                val ench = enchantments[index]
                enchantableButton(model, ench.maxLevel) { _, level ->
                    if (level > 0) options.items.first { it.type == Material.BOW }.addEnchantment(ench.enchantment, level)
                    else options.items.first { it.type == Material.BOW }.removeEnchantment(ench.enchantment)
                }.npc.changeItem(Material.BOW.toItemStack())
            }
        }
    }

    override fun onBack() { options.items.first { it.type == Material.BOW }.apply { removeAllEnchantments() } }
    override fun onReturn() { nextPage = Paginator.inferNextPage(player, 3) }
}