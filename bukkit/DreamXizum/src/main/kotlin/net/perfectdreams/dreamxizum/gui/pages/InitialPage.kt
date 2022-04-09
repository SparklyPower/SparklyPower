package net.perfectdreams.dreamxizum.gui.pages

import net.perfectdreams.dreamxizum.battle.Battle
import net.perfectdreams.dreamxizum.battle.BattleType
import net.perfectdreams.dreamxizum.gui.Paginator
import net.perfectdreams.dreamxizum.lobby.Lobby
import net.perfectdreams.dreamxizum.tasks.RankedQueueTask

class InitialPage : AbstractPage() {
    companion object {
        private var randomBattle = createBattle()
        private fun createBattle() = Battle(BattleType.NORMAL, 2).apply { options = RankedQueueTask.battleOptions }
    }

    init {
        button(npcs.create) {
            Paginator.fetch(it).addAndShowPage(MenuPage())
        }

        button(npcs.random) {
            Lobby.creatingBattle.remove(it)
            randomBattle.addToBattle(it)
            if (randomBattle.isFull) randomBattle = createBattle()
            Paginator.fetch(it).destroy()
        }
    }

}