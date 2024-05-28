package net.perfectdreams.loritta.morenitta.interactions.commands

import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions

abstract class LorittaSlashCommandExecutor {
    open val options: ApplicationCommandOptions = ApplicationCommandOptions.NO_OPTIONS

    abstract suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments)
}