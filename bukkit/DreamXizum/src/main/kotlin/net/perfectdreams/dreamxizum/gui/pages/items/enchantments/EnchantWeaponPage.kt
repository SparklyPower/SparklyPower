package net.perfectdreams.dreamxizum.gui.pages.items.enchantments

import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import net.perfectdreams.dreamxizum.gui.pages.items.ChooseArmorPage
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class EnchantWeaponPage(val options: BattleOptions, val item: ItemStack) : AbstractPage() {
    private val models = setOf(npcs.duckLoritta, npcs.frogPantufa, npcs.foxGabriela)
    private val enchantments = listOf(
        EnchantmentAndLevel(Enchantment.DAMAGE_ALL, 5),
        EnchantmentAndLevel(Enchantment.FIRE_ASPECT, 2),
        EnchantmentAndLevel(Enchantment.KNOCKBACK, 2)
    )

    init {
        onReturn()

        models.forEachIndexed { index, model ->
            val ench = enchantments[index]
            enchantableButton(model, ench.maxLevel) { _, level ->
                if (level > 0) item.meta<ItemMeta> { addEnchant(ench.enchantment, level, true) }
                else item.meta<ItemMeta> { removeEnchant(ench.enchantment) }
            }.npc.changeItem(item.clone())
        }
    }

    override fun onReturn() { nextPage = ChooseArmorPage(options) }
}