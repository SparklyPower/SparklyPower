package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedMentions
import net.perfectdreams.pantufa.PantufaBot

/**
 * Context of the executed command
 */
class ApplicationCommandContext(
    pantufa: PantufaBot,
    val event: GenericCommandInteractionEvent
) : InteractionContext(
    pantufa,
    UnleashedMentions(
        event.options.flatMap { it.mentions.users },
        event.options.flatMap { it.mentions.channels },
        event.options.flatMap { it.mentions.customEmojis },
        event.options.flatMap { it.mentions.roles }
    ),
    event
)