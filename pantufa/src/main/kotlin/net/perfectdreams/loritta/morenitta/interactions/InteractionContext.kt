package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData

abstract class InteractionContext {
    abstract val event: IReplyCallback
    val guildId
        get() = event.guild?.idLong

    val guildOrNull: Guild?
        get() = event.guild

    val guild: Guild
        get() = guildOrNull ?: error("This interaction was not sent in a guild!")

    val user
        get() = event.user

    var wasInitiallyDeferredEphemerally: Boolean? = null

    suspend fun deferChannelMessage(ephemeral: Boolean): InteractionHook {
        val hook = event.deferReply().setEphemeral(ephemeral).await()
        wasInitiallyDeferredEphemerally = ephemeral
        return hook
    }

    suspend inline fun reply(ephemeral: Boolean, content: String) = reply(ephemeral) {
        this.content = content
    }

    suspend inline fun reply(ephemeral: Boolean, builder: InlineMessage<MessageCreateData>.() -> Unit = {}) {
        val createdMessage = InlineMessage(MessageCreateBuilder()).apply(builder).build()

        // We could actually disable the components when their state expires, however this is hard to track due to "@original" or ephemeral messages not having an ID associated with it
        // So, if the message is edited, we don't know if we *can* disable the components when their state expires!

        if (event.isAcknowledged) {
            val message = event.hook.sendMessage(createdMessage).setEphemeral(ephemeral).await()
        } else {
            event.reply(createdMessage).setEphemeral(ephemeral).await()
            wasInitiallyDeferredEphemerally = ephemeral
        }
    }
}