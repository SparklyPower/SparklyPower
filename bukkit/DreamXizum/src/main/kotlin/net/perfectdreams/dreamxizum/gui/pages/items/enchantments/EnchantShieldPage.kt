package net.perfectdreams.dreamxizum.gui.pages.items.enchantments

import net.perfectdreams.dreamcore.utils.extensions.removeAllEnchantments
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

class EnchantShieldPage(val player: Player) : AbstractPage() {
    private val options = player.battle!!.options

    init {
        onReturn()

        enchantableButton(npcs.shieldUnbreakable, 3) { _, level ->
            if (level > 0) options.items.first { it.type == Material.SHIELD }.addEnchantment(Enchantment.DURABILITY, level)
            else options.items.first { it.type == Material.SHIELD }.removeEnchantment(Enchantment.DURABILITY)
        }.npc.changeItem(Material.SHIELD.toItemStack())
    }

    override fun onBack() { options.items.first { it.type == Material.SHIELD }.apply { removeAllEnchantments() } }
    override fun onReturn() { nextPage = Paginator.inferNextPage(player, 0) }
}