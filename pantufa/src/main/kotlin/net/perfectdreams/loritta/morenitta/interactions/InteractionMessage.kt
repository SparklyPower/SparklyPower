package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.pantufa.utils.extensions.await

interface InteractionMessage {
    suspend fun retrieveOriginal(): Message

    suspend fun editMessage(builder: suspend InlineMessage<MessageEditData>.() -> (Unit)): Message

    class InitialInteractionMessage(val hook: InteractionHook) : InteractionMessage {
        override suspend fun retrieveOriginal(): Message = hook.retrieveOriginal().await()

        override suspend fun editMessage(builder: suspend InlineMessage<MessageEditData>.() -> Unit): Message = hook.editOriginal(
            MessageEdit {
                builder()
            }
        ).await()
    }

    class FollowUpInteractionMessage(val message: Message) : InteractionMessage {
        override suspend fun retrieveOriginal(): Message = message

        override suspend fun editMessage(builder: suspend InlineMessage<MessageEditData>.() -> Unit): Message = message.editMessage(
            MessageEdit {
                builder()
            }
        ).await()
    }
}