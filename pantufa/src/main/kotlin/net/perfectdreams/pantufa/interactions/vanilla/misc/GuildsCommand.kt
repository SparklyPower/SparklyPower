package net.perfectdreams.pantufa.interactions.vanilla.misc

import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.api.commands.styled

class GuildsCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("guilds", "Veja todos os servidores em que eu estou!", CommandCategory.MISC) {
        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("guilds")
            add("servers")
        }

        executor = GuildsCommandExecutor()
    }

    inner class GuildsCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                styled(
                    "Eu estou nos seguintes servidores: ${context.jda.guilds.joinToString(", ") { "`${it.name}` (${it.memberCount} membros / ${it.memberCache.size()} membros em cache)" }} **(${context.jda.guilds.size})**",
                    Emotes.PantufaGaming.asMention
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }
}