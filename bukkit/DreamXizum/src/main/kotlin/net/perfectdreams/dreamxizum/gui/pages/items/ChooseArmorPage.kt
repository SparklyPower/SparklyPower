package net.perfectdreams.dreamxizum.gui.pages.items

import net.perfectdreams.dreamxizum.battle.BattleOptions
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import net.perfectdreams.dreamxizum.gui.pages.kits.PluginKitsPage

class ChooseArmorPage(val options: BattleOptions) : AbstractPage() {
    companion object {
        val goldenArmor = PluginKitsPage.getArmor("GOLDEN").toMutableSet()
    }

    init {
        button(npcs.armorLoritta) {
            Paginator.fetch(it).addAndShowPage(ExtrasPage(options, it))
        }

        button(npcs.armorPantufa) {
            Paginator.fetch(it).addAndShowPage(ArmorPage(options))
        }.npc.equipment = goldenArmor
    }

    override fun onReturn() { options.armor.clear() }
}