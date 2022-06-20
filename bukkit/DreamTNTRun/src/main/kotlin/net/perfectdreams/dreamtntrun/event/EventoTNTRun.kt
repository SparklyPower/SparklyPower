package net.perfectdreams.dreamtntrun.event

import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamtntrun.DreamTNTRun

class EventoTNTRun(val m: DreamTNTRun) : ServerEvent("TNT Run", "/tntrun") {
    init {
        this.requiredPlayers = 40
        this.delayBetween = 30 * 60 * 1_000 // 20 minutos entre cada evento
        this.command = "/tntrun"
    }

    override fun preStart() {
        // Se for começar um evento da Torre da Morte, mas já está acontecendo um evento, vamos apenas cancelar o atual
        if (m.TNTRun.isStarted) {
            if (m.TNTRun.isPreStart) {
                m.TNTRun.playersInQueue.toList().forEach { m.TNTRun.removeFromQueue(it) }
            } else {
                m.TNTRun.players.toList().forEach { m.TNTRun.removeFromGame(it, skipFinishCheck = false) }
            }
        }

        m.TNTRun.preStart(true)
    }
}