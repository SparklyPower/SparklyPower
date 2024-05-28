package net.perfectdreams.pantufa.commands.server.administration

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.LuckPermsGroupPermissions
import net.perfectdreams.pantufa.tables.LuckPermsUserPermissions
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.Server
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

open class RemoteCommandExecutorCommand(label: String,
										aliases:List<String> = listOf(),
										val requiredServerPermission: String,
										val commandToBeExecuted: String,
										val server: Server
) : AbstractCommand(label, aliases, true) {
	override fun run(context: CommandContext) {
		if (context.minecraftAccountInfo == null)
			return

		val userPerms = transaction(Databases.sparklyPowerLuckPerms) {
			LuckPermsUserPermissions.select {
				LuckPermsUserPermissions.uuid eq context.minecraftAccountInfo.uniqueId.toString()
			}.map { it[LuckPermsUserPermissions.permission] }
		}

		val groupNames = userPerms.filter { it.startsWith("group.") }
				.map { it.removePrefix("group.") }

		val allPermissions = groupNames.flatMap { getGroupPermissions(it) }

		if (requiredServerPermission !in allPermissions) {
			context.reply(
					PantufaReply(
							"Você por o acaso tem permissão para fazer isso? Não, né. Então pare de fazer perder meu tempo!"
					)
			)
			return
		}

		val payload = server.send(
				jsonObject(
						"type" to "executeCommand",
						"player" to context.minecraftAccountInfo.username,
						"command" to "$commandToBeExecuted " + context.args.joinToString(" ")
				)
		)

		val messages = payload["messages"].array.map { it.string }

		println("Message: $messages")

		// Now we are going to do some special checks
		if (messages.joinToString("\n").length >= 1900) { // if it is greater than 1900, we are going to send a file
			// (the reason it is 1900 is due to the formatting the replies do)
			context.event.channel.sendFiles(FileUpload.fromData(messages.joinToString("\n").toByteArray(Charsets.UTF_8), "result.txt")).complete()
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