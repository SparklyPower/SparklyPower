package net.perfectdreams.pantufa.listeners

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.utils.Constants
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

		m.launch {
			if (event.channel.idLong == Constants.SPARKLYPOWER_STAFF_CHANNEL_ID) {
				val payload = JsonObject()
				payload["type"] = "sendAdminChat"
				payload["player"] = event.author.name
				payload["message"] = event.message.contentRaw

				SocketUtils.sendAsync(payload, host = Constants.PERFECTDREAMS_BUNGEE_IP, port = Constants.PERFECTDREAMS_BUNGEE_PORT)
			}

			if (m.legacyCommandMap.dispatch(event))
				return@launch

			for (command in m.legacyCommandManager.commands)
				if (command.matches(event))
					return@launch
		}
	}
}