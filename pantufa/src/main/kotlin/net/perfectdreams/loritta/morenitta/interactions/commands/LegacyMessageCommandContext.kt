package net.perfectdreams.loritta.morenitta.interactions.commands

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.loritta.common.utils.text.TextUtils.convertMarkdownLinksWithLabelsToPlainLinks
import net.perfectdreams.loritta.morenitta.interactions.InteractionMessage
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedHook
import net.perfectdreams.loritta.morenitta.interactions.UnleashedMentions
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.extensions.await
import java.util.*

class LegacyMessageCommandContext(
    pantufa: PantufaBot,
    val event: MessageReceivedEvent,
    val args: List<String>,
    override val rootDeclaration: SlashCommandDeclaration,
    override val commandDeclaration: SlashCommandDeclaration,
) : UnleashedContext(
    pantufa,
    DiscordLocale.PORTUGUESE_BRAZILIAN,
    DiscordLocale.PORTUGUESE_BRAZILIAN,
    event.jda,
    UnleashedMentions(
        event.message.mentions.users,
        event.message.mentions.channels,
        event.message.mentions.customEmojis,
        event.message.mentions.roles
    ),
    event.author,
    event.member,
    event.guild,
    event.channel,
), CommandContext {
    override suspend fun deferChannelMessage(ephemeral: Boolean): UnleashedHook {
        event.channel.sendTyping().queue()

        return UnleashedHook.LegacyMessageHook()
    }

    override suspend fun reply(
        ephemeral: Boolean,
        builder: suspend InlineMessage<MessageCreateData>.() -> Unit
    ): InteractionMessage {
        val inlineBuilder = MessageCreate {
            allowedMentionTypes = EnumSet.of(
                Message.MentionType.CHANNEL,
                Message.MentionType.EMOJI,
                Message.MentionType.SLASH_COMMAND
            )

            builder()

            content = content?.convertMarkdownLinksWithLabelsToPlainLinks()
        }

        return InteractionMessage.FollowUpInteractionMessage(
            event.channel.sendMessage(inlineBuilder)
                .setMessageReference(event.message)
                .failOnInvalidReply(false)
                .await()
        )
    }
}