package net.perfectdreams.dreamxizum.gui.pages.items.enchantments

import net.perfectdreams.dreamcore.utils.extensions.removeAllEnchantments
import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

class EnchantTridentPage(val player: Player) : AbstractPage() {
    private val options = player.battle!!.options

    init {
        onReturn()

        enchantableButton(npcs.tridentLoyalty, 3) { _, level ->
            if (level > 0) options.items.first { it.type == Material.TRIDENT }.addEnchantment(Enchantment.LOYALTY, level)
            else options.items.first { it.type == Material.TRIDENT }.removeEnchantment(Enchantment.LOYALTY)
        }.npc.changeItem(Material.TRIDENT.toItemStack())
    }

    override fun onBack() { options.items.first { it.type == Material.TRIDENT }.apply { removeAllEnchantments() } }
    override fun onReturn() { nextPage = Paginator.inferNextPage(player, 1) }
}