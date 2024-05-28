package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext

/**
 * Context of the executed command
 */
class ApplicationCommandContext(
    override val event: SlashCommandInteractionEvent
) : InteractionContext()