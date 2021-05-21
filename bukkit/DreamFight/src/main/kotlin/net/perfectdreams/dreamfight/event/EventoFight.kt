package net.perfectdreams.dreamfight.event

import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamfight.DreamFight

class EventoFight(val m: DreamFight) : ServerEvent("Fight", "/fight") {
    init {
        this.requiredPlayers = 50
        this.delayBetween = 60 * 60 * 1_000 // 60 minutos entre cada evento
        this.command = "/fight"
        this.discordAnnouncementRole = 798697267312853002L
    }

    override fun preStart() {
        broadcastEventAnnouncement()
        m.fight.preStartFight()
    }
}