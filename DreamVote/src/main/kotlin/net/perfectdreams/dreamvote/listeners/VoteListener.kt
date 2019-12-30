package net.perfectdreams.dreamvote.listeners

import com.vexsoftware.votifier.model.VotifierEvent
import net.perfectdreams.dreamvote.DreamVote
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class VoteListener(val m: DreamVote) : Listener {
    @EventHandler
    fun onVote(e: VotifierEvent) {
        m.giveVoteAward(
                e.vote.username,
                e.vote.serviceName
        )
    }
}