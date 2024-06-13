package net.perfectdreams.dreamblockparty.event

import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamblockparty.DreamBlockParty

class EventoBlockParty(val m: DreamBlockParty) : ServerEvent("Block Party", "/blockparty") {
    init {
        this.requiredPlayers = 20
        this.delayBetween = 30 * 60 * 1_000 // 20 minutos entre cada evento
        this.command = "/blockparty"
    }

    override fun preStart() {
        // Se for começar um evento da Torre da Morte, mas já está acontecendo um evento, vamos apenas cancelar o atual
        if (m.blockParty.isStarted) {
            if (m.blockParty.isPreStart) {
                m.blockParty.playersInQueue.toList().forEach { m.blockParty.removeFromQueue(it) }
            } else {
                m.blockParty.players.toList().forEach { m.blockParty.removeFromGame(it, skipFinishCheck = false) }
            }
        }

        m.blockParty.preStart(true)
    }
}