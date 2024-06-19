package net.perfectdreams.pantufa.interactions.vanilla.magic

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.utils.Emotes
import net.perfectdreams.pantufa.api.commands.PantufaReply
import net.perfectdreams.pantufa.utils.Server
import net.perfectdreams.pantufa.api.commands.styled

class ExecuteCommand : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand("execute", "Executa algo no console", CommandCategory.MAGIC) {
        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("executar")
        }
        requireMinecraftAccount = true

        executor = ExecuteCommandExecutor()
    }

    inner class ExecuteCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val serverName = string("server_name", "Nome do servidor") {
                Server.servers.forEach {
                    choice(it.fancyName, it.internalName)
                }
            }
            val args = string("args", "Argumentos do comando")
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val serverName = args[options.serverName]

            if (context.user.id != "123170274651668480")
                return

            val server = Server.getByInternalName(serverName)

            if (server == null) {
                context.reply(true) {
                    styled(
                        "Servidores disponíveis: ${Server.servers.joinToString(", ", transform = { it.fancyName })}",
                        Emotes.PantufaPickaxe.toString()
                    )
                }
                return
            }

            val payload = server.send(
                jsonObject(
                    "type" to "executeCommand",
                    "pipeOutput" to true,
                    "command" to args[options.args]
                )
            )

            val messages = payload["messages"].array
            val replies = messages.map { PantufaReply(it.string) }

            context.reply(true) {
                replies.forEach {
                    styled(it)
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val serverName = args.getOrNull(0) ?: return null
            Server.getByInternalName(serverName) ?: return null
            val arguments = args.drop(1).joinToString( " ")

            return mapOf(
                options.serverName to serverName,
                options.args to arguments
            )
        }
    }
}