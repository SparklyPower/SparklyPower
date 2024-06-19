package net.perfectdreams.pantufa.interactions.vanilla.utils

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.pantufa.tables.AuthStorage
import net.perfectdreams.pantufa.api.commands.styled
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt

class ChangePassCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("changepass", "Altera a sua senha do SparklyPower", CommandCategory.UTILS) {
        requireMinecraftAccount = true
        executor = ChangePassCommandExecutor()
    }

    inner class ChangePassCommandExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val password = string("password", "Escreva a sua nova senha")
            val repeatPassword = string("repeat_password", "Escreva a sua nova senha novamente")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val account = context.retrieveConnectedMinecraftAccount()!!
            val password = args[options.password]
            val repeatPassword = args[options.repeatPassword]

            if (6 > password.length) {
                context.reply(true) {
                    styled(
                        "Sua senha é pequena demais! Você precisa criar uma senha com no mínimo 6 caracteres!"
                    )
                }
                return
            }

            if (password != repeatPassword) {
                context.reply(true) {
                    styled(
                        "As duas senhas não coincidem! Verifique as duas senhas e tente novamente!"
                    )
                }
                return
            }

            val minecraftId = account.uniqueId

            val salt = BCrypt.gensalt(12)
            val hashed = BCrypt.hashpw(password, salt)

            context.pantufa.transactionOnSparklyPowerDatabase {
                AuthStorage.update({ AuthStorage.uniqueId eq minecraftId }) {
                    it[AuthStorage.password] = hashed
                }
            }

            context.reply(true) {
                styled(
                    "A senha da sua conta foi alterada com sucesso! ^-^"
                )
                styled(
                    "Lembre-se, jamais compartilhe sua senha! Guarde ela com carinho em um lugar que você possa lembrar!"
                )
            }
        }
    }
}