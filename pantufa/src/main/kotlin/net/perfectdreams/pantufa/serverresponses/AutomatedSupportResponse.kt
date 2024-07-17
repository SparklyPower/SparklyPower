package net.perfectdreams.pantufa.serverresponses

import dev.minn.jda.ktx.messages.InlineMessage
import net.perfectdreams.pantufa.api.commands.PantufaReply

sealed class AutomatedSupportResponse {
    class AutomatedSupportPantufaReplyResponse(
        val replies: List<PantufaReply>
    ) : AutomatedSupportResponse()

    class AutomatedSupportMessageResponse(
        val messageBuilder: InlineMessage<*>.() -> (Unit)
    ) : AutomatedSupportResponse()
}