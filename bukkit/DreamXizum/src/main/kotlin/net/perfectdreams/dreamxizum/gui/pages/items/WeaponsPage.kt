package net.perfectdreams.dreamxizum.gui.pages.items

import net.perfectdreams.dreamcore.utils.extensions.toItemStack
import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import net.perfectdreams.dreamxizum.gui.pages.items.enchantments.EnchantWeaponPage
import org.bukkit.Material

class WeaponsPage(val items: List<Material>, val options: BattleOptions) : AbstractPage() {
    private val swordsNpcs = with (npcs) { listOf(swordsLoritta, swordsPantufa, swordsGabriela) }
    private val axesNpcs = with (npcs) { listOf(axesLoritta, axesPantufa, axesGabriela) }

    init {
        (if (items.first() == Material.IRON_SWORD) swordsNpcs else axesNpcs).apply {
            forEachIndexed { index, model ->
                val item = items[index].toItemStack()

                button(model) {
                    options.items.add(item)
                    Paginator.fetch(it).addAndShowPage(EnchantWeaponPage(options, item))
                }.npc.changeItem(item)
            }
        }
    }

    override fun onReturn() { options.items.removeFirstOrNull() }
}