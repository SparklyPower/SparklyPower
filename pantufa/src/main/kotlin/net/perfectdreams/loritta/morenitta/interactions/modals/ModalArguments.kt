package net.perfectdreams.loritta.morenitta.interactions.modals

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.perfectdreams.loritta.morenitta.interactions.commands.options.DiscordOptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.modals.options.DiscordModalOptionReference
import net.perfectdreams.loritta.morenitta.interactions.modals.options.ModalOptionReference

class ModalArguments(private val event: ModalInteractionEvent) {
    operator fun <T> get(argument: ModalOptionReference<T>): T {
        return when (argument) {
            is DiscordModalOptionReference -> {
                val option = event.getValue(argument.name)

                if (option == null) {
                    if (argument.required)
                        throw RuntimeException("Missing argument ${argument.name}!")

                    return null as T
                }

                return argument.get(option)
            }
        }
    }
}