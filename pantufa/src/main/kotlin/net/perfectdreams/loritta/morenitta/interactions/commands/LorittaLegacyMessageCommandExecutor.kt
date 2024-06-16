package net.perfectdreams.loritta.morenitta.interactions.commands

import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

interface LorittaLegacyMessageCommandExecutor {
    companion object {
        val NO_ARGS = emptyMap<OptionReference<*>, Any>()
    }

    suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>?
}