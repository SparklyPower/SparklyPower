package net.perfectdreams.pantufa.interactions.vanilla.minecraft

import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.api.commands.exceptions.SilentCommandException
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.api.minecraft.AccountResult
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.api.commands.styled
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class RegisterCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("registrar", "Registre-se no servidor!") {
        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("registrar")
            add("register")
        }

        executor = RegisterCommandExecutor()
    }

    inner class RegisterCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val username = string("username", "Seu nome dentro do servidor SparklyPower (ou seja, da sua conta do Minecraft)")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val username = args[options.username]

            context.pantufa.transactionOnSparklyPowerDatabase {
                DiscordAccounts.deleteWhere { discordId eq context.user.idLong }
            }

            val accountStatus = context.retrieveAccountStatus(username)

            if (accountStatus == AccountResult.UNKNOWN_PLAYER) {
                context.reply(false) {
                    styled(
                        "Usuário inexistente, você tem certeza que você colocou o nome certo?",
                        Constants.ERROR
                    )
                }

                throw SilentCommandException()
            }

            if (accountStatus == AccountResult.ALREADY_REGISTERED) {
                context.reply(false) {
                    styled(
                        "A conta que você deseja conectar já tem uma conta conectada no Discord! Para desregistrar, utilize `/discord desregistrar` no servidor!",
                        Constants.ERROR
                    )
                }

                throw SilentCommandException()
            }

            context.reply(false) {
                styled(
                    "Falta pouco! Para terminar a integração, vá no SparklyPower e utilize `/discord registrar` para terminar o registro!",
                    "<:lori_wow:626942886432473098>"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val arg0 = context.args.getOrNull(0)

            if (arg0 == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.username to arg0
            )
        }
    }
}