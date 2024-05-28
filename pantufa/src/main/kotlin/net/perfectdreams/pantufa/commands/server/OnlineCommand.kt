package net.perfectdreams.pantufa.commands.server

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import net.perfectdreams.pantufa.commands.AbstractCommand
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import net.perfectdreams.pantufa.utils.socket.SocketUtils

class OnlineCommand : AbstractCommand("online") {
	val serverToFancyName = mapOf(
			"sparklypower_lobby" to "SparklyPower Lobby",
			"sparklypower_survival" to "SparklyPower Survival"
	)

	override fun run(context: CommandContext) {
		val jsonObject = JsonObject()
		jsonObject["type"] = "getOnlinePlayersInfo"
		val response = SocketUtils.sendAsync(jsonObject, host = Constants.PERFECTDREAMS_BUNGEE_IP, port = Constants.PERFECTDREAMS_BUNGEE_PORT, success = { response ->
			val servers = response["servers"].array

			val replies = mutableListOf<PantufaReply>()

			val totalPlayersOnline = servers.sumBy {
				it["players"].array.size()
			}
			replies.add(
					PantufaReply(
							content = "**Players Online no SparklyPower Network ($totalPlayersOnline players online)**",
							prefix = "<a:pantufa_pickaxe:997671670468853770>"
					)
			)

			servers.forEach {
				val obj = it.obj
				val name = obj["name"].string
				val players = obj["players"].array.map {
					it["name"].string
				}.sorted()

				val fancyName = serverToFancyName[name]

				if (fancyName != null) {
					if (players.isNotEmpty()) {
						replies.add(
								PantufaReply(
										"**$fancyName (${players.size})**: ${players.joinToString(", ", transform = { "**`$it`**" })}",
										mentionUser = false
								)
						)
					} else {
						replies.add(
								PantufaReply(
										"**$fancyName (${players.size})**: Ninguém online... \uD83D\uDE2D",
										mentionUser = false
								)
						)
					}
				}
			}

			context.sendMessage(*replies.toTypedArray())
		}, error = { Constants.PERFECTDREAMS_OFFLINE.invoke(context) })
	}
}