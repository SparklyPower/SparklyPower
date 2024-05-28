package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.tables.AuthStorage
import net.perfectdreams.pantufa.utils.PantufaReply
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt

class ChangePassExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(
    pantufa
) {
    inner class Options : ApplicationCommandOptions() {
        val password = string("password", "Escreva a sua nova senha")
        val repeatPassword = string("repeat_password", "Escreva a sua nova senha novamente")
    }

    override val options = Options()

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val account = context.retrieveConnectedMinecraftAccountOrFail()

        val password = args[options.password]
        val repeatPassword = args[options.repeatPassword]

        if (6 > password.length) {
            context.sendEphemeralMessage {
                styled(
                    PantufaReply(
                        "Sua senha é pequena demais! Você precisa criar uma senha com no mínimo 6 caracteres!"
                    )
                )
            }
            return
        }

        if (6 > repeatPassword.length) {
            context.sendEphemeralMessage {
                styled(
                    PantufaReply(
                        "Sua senha é pequena demais! Você precisa criar uma senha com no mínimo 6 caracteres!"
                    )
                )
            }
            return
        }

        if (password != repeatPassword) { // senhas diferentes
            context.sendEphemeralMessage {
                styled(
                    PantufaReply(
                        "As duas senhas não coincidem! Verifique as duas senhas e tente novamente!"
                    )
                )
            }
            return
        }

        val minecraftId = account.uniqueId

        // Must be the same workload used in DreamAuth!
        val salt = BCrypt.gensalt(12)
        val hashed = BCrypt.hashpw(password, salt)

        pantufa.transactionOnSparklyPowerDatabase {
            AuthStorage.update({ AuthStorage.uniqueId eq minecraftId }) {
                it[AuthStorage.password] = hashed
            }
        }

        context.sendEphemeralMessage {
            styled(
                PantufaReply("A senha da sua conta foi alterada com sucesso! ^-^")
            )
            styled(
                PantufaReply("Lembre-se, jamais compartilhe sua senha! Guarde ela com carinho em um lugar que você possa lembrar!")
            )
        }
    }
}