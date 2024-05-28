package net.perfectdreams.loritta.morenitta.interactions.components

import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext

/**
 * Context of the executed command
 */
class ComponentContext(
    override val event: ComponentInteraction
) : InteractionContext() {
    /* suspend fun deferEdit(): InteractionHook = event.deferEdit().await()

    suspend fun sendModal(
        title: String,
        components: List<LayoutComponent>,
        callback: suspend (ModalContext, ModalArguments) -> (Unit)
    ) {
        val unleashedComponentId = UnleashedComponentId(UUID.randomUUID())
        loritta.interactivityManager.modalCallbacks[unleashedComponentId.uniqueId] = callback

        event.replyModal(
            unleashedComponentId.toString(),
            title,
            components
        ).await()
    } */
}