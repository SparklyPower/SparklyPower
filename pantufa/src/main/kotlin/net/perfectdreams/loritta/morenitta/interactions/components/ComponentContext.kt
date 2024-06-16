package net.perfectdreams.loritta.morenitta.interactions.components


import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEditBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedComponentId
import net.perfectdreams.loritta.morenitta.interactions.UnleashedHook
import net.perfectdreams.loritta.morenitta.interactions.UnleashedMentions
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.extensions.await
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Context of the executed command
 */
class ComponentContext(
    pantufa: PantufaBot,
    val event: ComponentInteraction
) : InteractionContext(pantufa, UnleashedMentions(emptyList(), emptyList(), emptyList(), emptyList()), event) {
    suspend fun deferEdit(): InteractionHook = event.deferEdit().await()

    /**
     * Edits the message that invoked the action
     */
    suspend inline fun editMessage(isReplace: Boolean = false, action: InlineMessage<*>.() -> (Unit)) = editMessage(isReplace, MessageEditBuilder { apply(action) }.build())

    /**
     * Edits the message that invoked the action
     */
    suspend inline fun editMessage(isReplace: Boolean = false, messageEditData: MessageEditData): UnleashedHook {
        return UnleashedHook.InteractionHook(event.editMessage(messageEditData).apply { this.isReplace = isReplace }.await())
    }

    /**
     * Defers the edit with [deferEdit] and edits the message with the result of the [action]
     */
    suspend inline fun deferAndEditOriginal(action: InlineMessage<*>.() -> (Unit)) = deferAndEditOriginal(MessageEditBuilder { apply(action) }.build())

    /**
     * Defers the edit with [deferEdit] and edits the message with the result of the [action]
     */
    suspend inline fun deferAndEditOriginal(messageEditData: MessageEditData): Message {
        val hook = deferEdit()

        return hook.editOriginal(messageEditData).await()
    }

    /**
     * Edits the message in an async manner
     *
     * The edit is "async" because the job is submitted instead of awaited, useful if you want to edit the message while doing other work in parallel
     */
    inline fun editMessageAsync(action: InlineMessage<*>.() -> (Unit)) = editMessageAsync(MessageEditBuilder { apply(action) }.build())

    /**
     * Edits the message in an async manner
     *
     * The edit is "async" because the job is submitted instead of awaited, useful if you want to edit the message while doing other work in parallel
     */
    fun editMessageAsync(messageEditData: MessageEditData): CompletableFuture<InteractionHook> {
        return event.editMessage(messageEditData).submit()
    }

    /**
     * Invalidates the component callback
     *
     * If this component is invocated in the future, it will fail with "callback not found"!
     *
     * Useful if you are making a "one shot" component
     */
    fun invalidateComponentCallback() {
        val componentId = UnleashedComponentId(event.componentId)

        when (event.componentType) {
            Component.Type.UNKNOWN -> TODO()
            Component.Type.ACTION_ROW -> TODO()
            Component.Type.BUTTON -> {
                pantufa.interactivityManager.buttonInteractionCallbacks.remove(componentId.uniqueId)
            }
            Component.Type.STRING_SELECT -> {
                pantufa.interactivityManager.selectMenuInteractionCallbacks.remove(componentId.uniqueId)
            }
            Component.Type.TEXT_INPUT -> TODO()
            Component.Type.USER_SELECT -> TODO()
            Component.Type.ROLE_SELECT -> TODO()
            Component.Type.MENTIONABLE_SELECT -> TODO()
            Component.Type.CHANNEL_SELECT -> TODO()
        }
    }

    suspend fun sendModal(
        title: String,
        components: List<LayoutComponent>,
        callback: suspend (ModalContext, ModalArguments) -> Unit
    ) {
        val unleashedComponentId = UnleashedComponentId(UUID.randomUUID())
        pantufa.interactivityManager.modalCallbacks[unleashedComponentId.uniqueId] = callback

        event.replyModal(
            unleashedComponentId.toString(),
            title,
            components
        ).await()
    }
}