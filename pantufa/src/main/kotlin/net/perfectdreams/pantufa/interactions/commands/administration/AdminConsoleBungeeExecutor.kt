package net.perfectdreams.pantufa.interactions.commands.administration

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.interactions.commands.PantufaCommandContext
import net.perfectdreams.pantufa.interactions.commands.PantufaInteractionCommand
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.LuckPermsGroupPermissions
import net.perfectdreams.pantufa.tables.LuckPermsUserPermissions
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.Server
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

open class AdminConsoleBungeeExecutor(
    pantufa: PantufaBot,
    val requiredServerPermission: String,
    val commandToBeExecuted: String,
    val server: Server
) : PantufaInteractionCommand(
    pantufa,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    inner class Options : ApplicationCommandOptions() {
        val args = string("args", "Argumentos do comando")
    }

    override val options = Options()

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        context.interactionContext.deferChannelMessage()

        logger.info { "Retrieving ${context.senderId}'s Minecraft Account (or failing if not present)..." }
        val minecraftAccountInfo = context.retrieveConnectedMinecraftAccountOrFail()

        logger.info { "Getting ${context.senderId}'s Minecraft Account (${minecraftAccountInfo.uniqueId}) permissions..." }
        val userPerms = transaction(Databases.sparklyPowerLuckPerms) {
            LuckPermsUserPermissions.select {
                LuckPermsUserPermissions.uuid eq minecraftAccountInfo.uniqueId.toString()
            }.map { it[LuckPermsUserPermissions.permission] }
        }

        val groupNames = userPerms.filter { it.startsWith("group.") }
            .map { it.removePrefix("group.") }

        val allPermissions = groupNames.flatMap { getGroupPermissions(it) }

        logger.info { "Does ${context.senderId}'s Minecraft Account (${minecraftAccountInfo.uniqueId}) has permission $requiredServerPermission? ${requiredServerPermission !in allPermissions}" }
        if (requiredServerPermission !in allPermissions) {
            context.reply(
                PantufaReply(
                    "Você por o acaso tem permissão para fazer isso? Não, né. Então pare de fazer perder meu tempo!"
                )
            )
            return
        }

        logger.info { "Executing ${context.senderId}'s command in SparklyPower's BungeeCord with Minecraft Account (${minecraftAccountInfo.uniqueId})..." }
        val payload = server.send(
            jsonObject(
                "type" to "executeCommand",
                "player" to minecraftAccountInfo.username,
                "command" to "$commandToBeExecuted ${args[options.args]}"
            )
        )

        val messages = payload["messages"].array.map { it.string }

        println("Message: $messages")

        // Now we are going to do some special checks
        if (messages.joinToString("\n").length >= 1900) { // if it is greater than 1900, we are going to send a file
            // (the reason it is 1900 is due to the formatting the replies do)
            context.sendMessage {
                addFile("result.txt", messages.joinToString("\n").toByteArray(Charsets.UTF_8).inputStream())
            }
        } else {
            var isFirst = true

            val replies = messages.map {
                val reply = PantufaReply(it, mentionUser = isFirst)
                isFirst = false
                reply
            }

            context.reply(
                *replies.toTypedArray()
            )
        }
    }

    private fun getGroupPermissions(groupName: String): List<String> {
        val perms = mutableListOf<String>()

        val permissions = transaction(Databases.sparklyPowerLuckPerms) {
            LuckPermsGroupPermissions.select {
                LuckPermsGroupPermissions.name eq groupName
            }.map { it[LuckPermsGroupPermissions.permission] }
        }

        perms.addAll(permissions)

        perms.addAll(
            permissions.filter { it.startsWith("group.") }
                .map { it.removePrefix("group.") }
                .flatMap { getGroupPermissions(it) }
        )

        return perms
    }
}