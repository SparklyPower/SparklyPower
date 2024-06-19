package net.perfectdreams.loritta.morenitta.interactions.commands

import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

class SlashCommandArguments(private val event: SlashCommandArgumentsSource) {
    operator fun <T> get(argument: OptionReference<T>): T = event[argument]
}