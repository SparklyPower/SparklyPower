package net.perfectdreams.pantufa.serverresponses

import net.perfectdreams.pantufa.api.commands.PantufaReply

data class AutomatedSupportResponse(
    val replies: List<PantufaReply>
)