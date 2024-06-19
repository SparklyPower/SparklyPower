package net.perfectdreams.pantufa.interactions.vanilla.utils

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.utils.Server
import net.perfectdreams.pantufa.utils.formatToTwoDecimalPlaces
import net.perfectdreams.pantufa.api.commands.styled

class TPSCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("tps", "Veja o TPS na rede de servidores do SparklyPower!") {
        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("tps")
        }

        executor = TPSCommandExecutor()
    }

    inner class TPSCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val server = string("server", "O servidor que deseja consultar!") {
                Server.servers.forEach {
                    choice(it.name, it.internalName)
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val serverName = args[options.server]

            val server = Server.getByInternalName(serverName)

            if (server == null) {
                context.reply(true) {
                    styled(
                        Server.servers.joinToString(", ", transform = { it.internalName })
                    )
                }

                return
            }

            val payload = server.send(
                jsonObject(
                    "type" to "getTps"
                )
            )

            println(payload)

            val tps = payload["tps"].array

            context.reply(false) {
                styled(
                    "Atualmente ${server.internalName} está com ${tps[0].double.formatToTwoDecimalPlaces()} TPS!"
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val serverName = args.getOrNull(0)

            if (serverName == null) {
                context.reply(true) {
                    styled(
                        Server.servers.joinToString(", ", transform = { it.internalName })
                    )
                }

                return null
            }

            return mapOf(
                options.server to serverName
            )
        }
    }
}