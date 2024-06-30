package net.perfectdreams.pantufa.listeners

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.api.commands.PantufaReply
import net.perfectdreams.pantufa.utils.Server
import net.perfectdreams.pantufa.utils.extensions.await
import net.perfectdreams.pantufa.utils.socket.SocketUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DiscordListener(val m: PantufaBot) : ListenerAdapter() {
	override fun onGuildBan(event: GuildBanEvent) {
		m.launch {
			val user = m.getDiscordAccountFromUser(event.user) ?: return@launch
			val sparklyUsername = transaction(Databases.sparklyPower) { User.findById(user.minecraftId)?.username } ?: return@launch

			val userBan = try {
				event.guild.retrieveBan(event.user)
						.await()
			} catch (e: Exception) { return@launch }

			Server.PERFECTDREAMS_BUNGEE.send(
					jsonObject(
							"type" to "executeCommand",
							"player" to "Pantufa",
							"command" to "ban $sparklyUsername Banido no Discord do SparklyPower: ${userBan.reason}"
					)
			)
		}
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.author.isBot)
			return

		if (event.message.type != MessageType.DEFAULT && event.message.type != MessageType.INLINE_REPLY)
			return

		m.launchMessageJob(event) {
			val sparklyPower = m.config.sparklyPower

			if (event.channel.idLong == sparklyPower.guild.staffChannelId) {
				val payload = JsonObject()
				payload["type"] = "sendAdminChat"
				payload["player"] = event.author.name
				payload["message"] = event.message.contentRaw

				SocketUtils.sendAsync(
					payload,
					host = sparklyPower.server.perfectDreamsBungeeIp,
					port = sparklyPower.server.perfectDreamsBungeePort
				)
			}

			if (checkCommandsAndDispatch(event))
				return@launchMessageJob
		}
	}

	suspend fun checkCommandsAndDispatch(event: MessageReceivedEvent): Boolean {
		// If Pantufa can't speak in the current channel, do *NOT* try to process a command! If we try to process, Pantufa will have issues that she wants to talk in a channel, but she doesn't have the "canTalk()" permission!
		if (event.channel is TextChannel && !event.channel.canTalk())
			return false

		val rawMessage = event.message.contentRaw
		val rawArguments = rawMessage
			.split(" ")
			.toMutableList()

		val firstLabel = rawArguments.first()
		val startsWithCommandPrefix = firstLabel.startsWith(PantufaBot.PREFIX)
		val startsWithPantufaMention = firstLabel == "<@${m.jda.selfUser.id}>" || firstLabel == "<@!${m.jda.selfUser.id}>"

		// If the message starts with the prefix, it could be a command!
		if (startsWithCommandPrefix || startsWithPantufaMention) {
			if (startsWithCommandPrefix) // If it is a command prefix, remove the prefix
				rawArguments[0] = rawArguments[0].removePrefix(PantufaBot.PREFIX)
			else if (startsWithPantufaMention) { // If it is a mention, remove the first argument (which is Pantufa's mention)
				rawArguments.removeAt(0)
				if (rawArguments.isEmpty()) // If it is empty, then it means that it was only Pantufa's mention, so just return false
					return false
			}

			// Executar comandos
			if (m.interactionsListener.manager.matches(event, rawArguments)) {
				return true
			}
		}

		return false
	}
}