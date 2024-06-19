package net.perfectdreams.loritta.morenitta.interactions.modals

import net.dv8tion.jda.api.interactions.modals.ModalInteraction
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedHook
import net.perfectdreams.loritta.morenitta.interactions.UnleashedMentions
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.extensions.await

class ModalContext (
    pantufa: PantufaBot,
    val event: ModalInteraction
) : InteractionContext(
    pantufa,
    UnleashedMentions(
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList()
    ),
    event
) {
    suspend fun deferEdit() = UnleashedHook.InteractionHook(event.deferEdit().await())
}