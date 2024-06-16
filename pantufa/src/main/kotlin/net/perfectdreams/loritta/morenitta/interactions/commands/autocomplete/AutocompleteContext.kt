package net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.perfectdreams.pantufa.PantufaBot

class AutocompleteContext(
    val pantufa: PantufaBot,
    val event: CommandAutoCompleteInteractionEvent
)