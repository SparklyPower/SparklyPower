package net.perfectdreams.dreamxizum.gui.pages.items.enchantments

import net.perfectdreams.dreamcore.utils.extensions.removeAllEnchantments
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import net.perfectdreams.dreamxizum.gui.pages.items.ExtrasPage
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class EnchantArmorPage(val armor: MutableSet<ItemStack>, val player: Player) : AbstractPage() {
    private val options = player.battle!!.options
    private val enchantments = listOf(
        EnchantmentAndLevel(Enchantment.PROTECTION_ENVIRONMENTAL, 4),
        EnchantmentAndLevel(Enchantment.THORNS, 3),
        EnchantmentAndLevel(Enchantment.DURABILITY, 3)
    )

    init {
        onReturn()

        with (npcs) { listOf(protectionArmor, thornsArmor, unbreakableArmor) }.forEachIndexed { index, model ->
            val ench = enchantments[index]
            enchantableButton(model, ench.maxLevel) { _, level ->
                if (level > 0) armor.forEach { it.addEnchantment(ench.enchantment, level) }
                else armor.forEach { it.removeEnchantment(ench.enchantment) }
            }.npc.equipment = mutableSetOf<ItemStack>().apply {
                addAll(armor)
                forEach { it.removeAllEnchantments() }
            }
        }
    }

    override fun onBack() { options.armor = options.armor.mapTo(mutableSetOf()) { it.apply { removeAllEnchantments() } } }
    override fun onReturn() { nextPage = ExtrasPage(options, player) }
}