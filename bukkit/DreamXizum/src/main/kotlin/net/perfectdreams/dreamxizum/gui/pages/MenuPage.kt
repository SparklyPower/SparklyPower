package net.perfectdreams.dreamxizum.gui.pages

import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.battle.BattleType
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.items.ItemsPage

class MenuPage : AbstractPage() {
    init {
        with (npcs) { listOf(x1, x2, x3) }.forEachIndexed { index, model ->
            button(model) {
                Paginator.fetch(it).addAndShowPage(
                    ItemsPage(Matchmaker.createBattle(BattleType.NORMAL, (index + 1) * 2, it))
                )
            }
        }
    }
}