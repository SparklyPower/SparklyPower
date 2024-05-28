package net.perfectdreams.loritta.morenitta.interactions.modals

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.modals.ModalInteraction
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext

/**
 * Context of the executed command
 */
class ModalContext(
    override val event: ModalInteraction
) : InteractionContext() {
    suspend fun deferEdit(): InteractionHook = event.deferEdit().await()
}