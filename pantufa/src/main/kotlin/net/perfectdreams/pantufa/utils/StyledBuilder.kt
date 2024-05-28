package net.perfectdreams.pantufa.utils

import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.serialization.json.JsonNull.content
import net.perfectdreams.loritta.cinnamon.emotes.Emotes

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param content the content of the styled message
 * @param prefix  the emote prefix of the styled message
 */
fun InlineMessage<*>.styled(content: String, prefix: String = Emotes.DefaultStyledPrefix.asMention) = styled(
    PantufaReply(content, prefix)
)

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param reply the already built LorittaReply
 */
fun InlineMessage<*>.styled(reply: PantufaReply) {
    val styled = createStyledContent(reply)

    if (content != null) {
        content += "\n"
        content += styled
    } else {
        content = styled
    }
}

/**
 * Creates a Loritta-styled formatted content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * @param content the content of the styled message
 * @param prefix  the emote prefix of the styled message
 */
fun createStyledContent(content: String, prefix: String = Emotes.DefaultStyledPrefix.asMention) = createStyledContent(
    PantufaReply(content, prefix)
)

/**
 * Creates a Loritta-styled formatted content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * @param reply the already built LorittaReply
 */
fun createStyledContent(reply: PantufaReply) = "${reply.prefix} **|** ${reply.content}"