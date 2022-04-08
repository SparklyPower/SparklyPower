package net.perfectdreams.dreamxizum.gui.pages.items

import net.perfectdreams.dreamxizum.battle.Battle
import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.battle.BattleItems
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage
import net.perfectdreams.dreamxizum.gui.pages.FinalPage
import net.perfectdreams.dreamxizum.gui.pages.kits.KitsPage
import org.bukkit.Material

class ItemsPage(val battle: Battle) : AbstractPage() {
    init {
        button(npcs.custom) {
            Paginator.fetch(it).addAndShowPage(ChooseWeaponPage(it.battle!!.options))
            it.battle!!.options.itemsType = BattleItems.CUSTOM_ITEMS
        }.npc.changeItem(Material.GOLDEN_PICKAXE, true)

        button(npcs.kits) {
            it.battle!!.options.itemsType = BattleItems.PLUGIN_KITS
            Paginator.fetch(it).addAndShowPage(KitsPage(it.battle!!.options))
        }

        button(npcs.items) {
            it.battle!!.options.itemsType = BattleItems.PLAYER_ITEMS
            Paginator.fetch(it).addAndShowPage(FinalPage(it))
        }.npc.changeItem(Material.GOLDEN_SHOVEL)
    }

    override fun onBack() { Matchmaker.cancelBattle(battle) }
}