package net.perfectdreams.dreamxizum.gui.pages.betting

import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.gui.pages.AbstractPage

class BettingPage : AbstractPage() {
    init {
        button(npcs.betSonecas) {
            Paginator.fetch(it).addAndShowPage(AdjustSonecasPage(it))
        }

        button(npcs.betCash) {
            Paginator.fetch(it).addAndShowPage(AdjustCashPage(it))
        }
    }
}