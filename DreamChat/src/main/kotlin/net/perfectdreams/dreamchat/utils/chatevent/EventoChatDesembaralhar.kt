package net.perfectdreams.dreamchat.utils.chatevent

import net.perfectdreams.dreamchat.dao.EventMessage
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.extensions.centralize
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class EventoChatDesembaralhar : IEventoChat {
	val words = listOf(
		"AMOR",
		"MITO",
		"FATO",
		"SEDE",
		"RUIM",
		"BOM",
		"VIDA",
		"MEDO",
		"RIMA",
		"FASE",
		"MÃE",
		"SIM",
		"NÃO",
		"META",
		"SAIR",
		"LORITTA",
		"PANTUFA"
	)

	var currentWord: String? = null

	override fun preStart() {
		currentWord = words.random()
	}

	override fun getAnnouncementMessage(): String {
		val currentWord = currentWord!!

		var shuffledChars = currentWord.toCharArray().toList()

		while (shuffledChars.joinToString("") == currentWord)
			shuffledChars = shuffledChars.shuffled()

		val shuffledWord = shuffledChars.joinToString(separator = "")

		return shuffledWord
	}

	override fun getToDoWhat(): String {
		return "desembaralhar"
	}

	@Synchronized
	override fun process(player: Player, message: String): Boolean {
		if (currentWord == null)
			return false

		return message.equals(currentWord, true)
	}
}