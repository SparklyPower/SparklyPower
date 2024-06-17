package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.pantufa.PantufaBot
import java.util.*

abstract class InteractionContext(
    pantufa: PantufaBot,
    mentions: UnleashedMentions,
    private val replyCallback: IReplyCallback
) : UnleashedContext(
    pantufa,
    if (replyCallback.isFromGuild) replyCallback.guildLocale else null,
    replyCallback.userLocale,
    replyCallback.jda,
    mentions,
    replyCallback.user,
    replyCallback.member,
    replyCallback.guild,
    replyCallback.messageChannel
) {
    override suspend fun deferChannelMessage(ephemeral: Boolean): UnleashedHook {
        val hook = replyCallback.deferReply().setEphemeral(ephemeral).await()
        wasInitiallyDeferredEphemerally = ephemeral
        return UnleashedHook.InteractionHook(hook)
    }

    override suspend fun reply(
        ephemeral: Boolean,
        builder: suspend InlineMessage<MessageCreateData>.() -> Unit
    ): InteractionMessage {
        val createdMessage = InlineMessage(MessageCreateBuilder()).apply {
            allowedMentionTypes = EnumSet.of(
                Message.MentionType.CHANNEL,
                Message.MentionType.EMOJI,
                Message.MentionType.SLASH_COMMAND
            )

            builder()
        }.build()

        return if (replyCallback.isAcknowledged) {
            val message = replyCallback.hook.sendMessage(createdMessage)
                .setEphemeral(ephemeral)
                .await()
            InteractionMessage.FollowUpInteractionMessage(message)
        } else {
            val hook = replyCallback.reply(createdMessage)
                .setEphemeral(ephemeral)
                .await()
            InteractionMessage.InitialInteractionMessage(hook)
        }
    }
}