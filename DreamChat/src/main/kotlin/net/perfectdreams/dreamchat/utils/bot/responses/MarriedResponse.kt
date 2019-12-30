package net.perfectdreams.dreamchat.utils.bot.responses

import net.perfectdreams.dreamcore.utils.onlinePlayers
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.regex.Pattern

class MarriedResponse : RegExResponse() {
	init {
		patterns.add("quem".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("casad".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("com".toPattern(Pattern.CASE_INSENSITIVE))
	}

	override fun postHandleResponse(message: String, event: AsyncPlayerChatEvent): Boolean {
		for (player in onlinePlayers()) {
			if (message.contains(player.name) || message.contains(player.displayName)) {
				return true
			}
		}
		return false
	}

	override fun getResponse(message: String, event: AsyncPlayerChatEvent): String? {
		/* for (player in onlinePlayers()) {
			if (message.contains(player.name) || message.contains(player.displayName)) {
				val casal = CasamentoAPI.INSTANCE.getCasalByPlayer(player.name) ?: return "§a" + event.player.displayName + "§a, ${player.artigo} §b${player.displayName}§a não está casad${player.artigo}!"

				return "§a" + event.player.displayName + "§a, ${player.artigo} §b${player.displayName}§a está casad${player.artigo} com §b${casal.getPartnerOf(player)}§a!"
			}
		} */

		return "???"
	}
}