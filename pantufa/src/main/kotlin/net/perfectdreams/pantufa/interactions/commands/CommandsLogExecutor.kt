package net.perfectdreams.pantufa.interactions.commands

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.Command
import net.perfectdreams.pantufa.interactions.components.utils.MessagePanelType
import net.perfectdreams.pantufa.interactions.components.utils.buildCommandsLogMessage
import net.perfectdreams.pantufa.interactions.components.utils.invalidPageMessage
import net.perfectdreams.pantufa.interactions.components.utils.saveAndCreateData
import net.perfectdreams.pantufa.network.Databases
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CommandsLogExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(pantufa) {
    inner class Options : ApplicationCommandOptions() {
        val player = optionalString("player", "Nome do jogador")
        val world = optionalString("world", "Mundo em que o comando foi usado")
        val alias = optionalString("alias", "Comando usado")
        val page = optionalInteger("page", "A página que você quer visualizar")
        var args = optionalString("args", "Argumentos usados dentro do comando")
    }

    override val options = Options()

    private val staffRoleId = Snowflake(332650495522897920)

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        if (staffRoleId !in context.interactionContext.member.roleIds) {
            context.sendEphemeralMessage {
                content = "<:pantufa_analise:853048446813470762> **|** Você não pode usar esse comando."
            }
            return
        }

        val fetchedCommands = Command.fetchCommands(
            args[options.player],
            args[options.world],
            args[options.alias],
            args[options.args]
        )

        val size = transaction(Databases.sparklyPower) { fetchedCommands.count() }

        val page = args[options.page]?.let {
            if (it < 1 || it * MessagePanelType.COMMANDS_LOG.entriesPerPage > size) return context.reply(invalidPageMessage)
            it - 1
        } ?: 0

        val arguments = mutableListOf<String>().apply {
            args[options.player]?.let { add(":person_bowing: **Jogador**: `$it`") }
            args[options.world]?.let { add(":earth_americas: **Mundo**: `$it`") }
            args[options.alias]?.let { add(":computer: **Comando**: `/$it`") }
            args[options.args]?.let { add(":pencil: **Argumentos**: `$it`") }
        }

        val messageData = saveAndCreateData(
            size,
            context.sender.id,
            UUID.randomUUID(),
            MessagePanelType.COMMANDS_LOG,
            fetchedCommands
        )

        context.sendMessage {
            messageData.buildCommandsLogMessage(page).let {
                embeds = it.embeds
                components = it.components
            }
        }
    }
}