package net.perfectdreams.pantufa.interactions.vanilla.utils

import net.dv8tion.jda.api.entities.Activity
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.api.commands.styled

class VerificarCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("verificar", "TODOFIXTHISDATA", CommandCategory.UTILS) {
        enableLegacyMessageSupport = true
        requireMinecraftAccount = true

        subcommand("status", "Coloque seu status do Discord como \"mc.sparklypower.net\" para ganhar Sonecas por minuto!") {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("verificar status")
                add("verificarstatus")
            }
            executor = VerificarStatusExecutor()
        }
    }

    inner class VerificarStatusExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val customStatus = context.memberOrNull?.activities?.firstOrNull { it.type == Activity.ActivityType.CUSTOM_STATUS }

            if (customStatus == null) {
                context.reply(true) {
                    styled(
                        "Você não possui nenhum status!",
                        Constants.ERROR
                    )
                }
                return
            }

            if (customStatus.name.contains("mc.sparklypower.net") || customStatus.name.contains("discord.gg/sparklypower")) {
                context.reply(true) {
                    styled(
                        "Certinho! Você irá ganhar 15 sonecas por minuto enquanto o seu status estiver ativo! Obrigada por ajudar a divulgar o servidor, seu foof ;3",
                        "<:pantufa_thumbsup:853048446826840104>"
                    )
                }
            } else {
                context.reply(true) {
                    styled(
                        "Você precisa colocar `mc.sparklypower.net` ou `discord.gg/sparklypower` no seu status para ganhar os sonecas! Aliás, seja criativo no status! Que tal colocar `Survival 1.16.3: mc.sparklypower.net | Amo esse servidor muito daora e foof!`?",
                        Constants.ERROR
                    )
                }
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