package net.perfectdreams.dreamtorredamorte.event

import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte

class EventoTorreDaMorte(val m: DreamTorreDaMorte) : ServerEvent("Torre da Morte", "/torre") {
    init {
        this.requiredPlayers = 20
        this.delayBetween = 20 * 60 * 1_000 // 20 minutos entre cada evento
        this.command = "/torre"
    }

    override fun preStart() {
        // Se for começar um evento da Torre da Morte, mas já está acontecendo um evento, vamos apenas cancelar o atual
        if (m.torreDaMorte.isStarted) {
            if (m.torreDaMorte.isPreStart) {
                m.torreDaMorte.playersInQueue.toList().forEach { m.torreDaMorte.removeFromQueue(it) }
            } else {
                m.torreDaMorte.players.toList().forEach { m.torreDaMorte.removeFromGame(it, skipFinishCheck = false) }
            }
        }

        m.torreDaMorte.preStart(true)
    }
}