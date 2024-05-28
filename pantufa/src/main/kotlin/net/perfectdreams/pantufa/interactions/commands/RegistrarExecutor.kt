package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.SilentCommandException
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.tables.DiscordAccounts
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class RegistrarExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    inner class Options : ApplicationCommandOptions() {
        val username = string("username", "Seu nome no SparklyPower (ou seja, da sua conta do Minecraft)")
    }

    override val options = Options()

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val arg0 = args[options.username]

        pantufa.transactionOnSparklyPowerDatabase {
            DiscordAccounts.deleteWhere { DiscordAccounts.discordId eq context.senderId.toLong() }
        }

        val accountStatus = pantufa.transactionOnSparklyPowerDatabase {
            val user = User.find { Users.username eq arg0 }.firstOrNull()
                ?: return@transactionOnSparklyPowerDatabase AccountResult.UNKNOWN_PLAYER

            val connectedAccounts = DiscordAccount.find {
                DiscordAccounts.minecraftId eq user.id.value and (DiscordAccounts.isConnected eq true)
            }.count()

            if (connectedAccounts != 0L)
                return@transactionOnSparklyPowerDatabase AccountResult.ALREADY_REGISTERED

            DiscordAccount.new {
                this.minecraftId = user.id.value
                this.discordId = context.senderId.toLong()
                this.isConnected = false
            }
            return@transactionOnSparklyPowerDatabase AccountResult.OK
        }

        if (accountStatus == AccountResult.UNKNOWN_PLAYER) {
            context.reply(
                PantufaReply(
                    "Usuário inexistente, você tem certeza que você colocou o nome certo?",
                    Constants.ERROR
                )
            )
            throw SilentCommandException()
        }

        if (accountStatus == AccountResult.ALREADY_REGISTERED) {
            context.reply(
                PantufaReply(
                    "A conta que você deseja conectar já tem uma conta conectada no Discord! Para desregistrar, utilize `/discord desregistrar` no servidor!",
                    Constants.ERROR
                )
            )
            throw SilentCommandException()
        }

        context.reply(
            PantufaReply(
                "Falta pouco! Para terminar a integração, vá no SparklyPower e utilize `/discord registrar` para terminar o registro!",
                "<:lori_wow:626942886432473098>"
            )
        )
    }

    enum class AccountResult {
        OK,
        UNKNOWN_PLAYER,
        ALREADY_REGISTERED
    }
}