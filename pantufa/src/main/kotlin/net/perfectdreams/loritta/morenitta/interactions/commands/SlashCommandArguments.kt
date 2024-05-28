package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.perfectdreams.loritta.morenitta.interactions.commands.options.DiscordOptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

class SlashCommandArguments(private val event: SlashCommandInteractionEvent) {
    operator fun <T> get(argument: OptionReference<T>): T {
        return when (argument) {
            is DiscordOptionReference -> {
                val option = event.getOption(argument.name)

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