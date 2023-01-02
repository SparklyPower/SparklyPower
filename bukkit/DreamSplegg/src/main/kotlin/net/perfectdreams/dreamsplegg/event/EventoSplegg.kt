package net.perfectdreams.dreamsplegg.event

import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamsplegg.DreamSplegg

class EventoSplegg(val m: DreamSplegg) : ServerEvent("Splegg", "/splegg") {
    init {
        this.requiredPlayers = 55
        this.delayBetween = 30 * 60 * 1_000 // 20 minutos entre cada evento
        this.command = "/splegg"
    }

    override fun preStart() {
        // Se for começar um evento da Torre da Morte, mas já está acontecendo um evento, vamos apenas cancelar o atual
        if (m.splegg.isStarted) {
            if (m.splegg.isPreStart) {
                m.splegg.playersInQueue.toList().forEach { m.splegg.removeFromQueue(it) }
            } else {
                m.splegg.players.toList().forEach { m.splegg.removeFromGame(it, skipFinishCheck = false) }
            }
        }

        m.splegg.preStart(true)
    }
}