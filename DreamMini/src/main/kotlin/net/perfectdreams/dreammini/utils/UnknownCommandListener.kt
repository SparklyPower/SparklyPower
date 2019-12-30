package net.perfectdreams.dreammini.utils

import net.perfectdreams.dreammini.DreamMini
import org.apache.commons.lang3.StringUtils
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class UnknownCommandListener(val m: DreamMini) : Listener {
	@EventHandler
	fun unknownCommand(e: PlayerCommandPreprocessEvent) {
		val known = Bukkit.getCommandMap().knownCommands
		val cmd = e.message.split(" ")[0].substring(1)
		if (known.keys.contains(cmd))
			return

		var mostSimilarCommand: String? = null
		var distance = 999999

		for (test in known.entries) {
			if (test.key.equals(cmd, true))
				return

			if (test.value.aliases.contains(cmd))
				return

			if (test.key.contains(":"))
				continue

			if (test.value.permission != null && !e.player.hasPermission(test.value.permission))
				continue

			val theCmd = test.key
			val now = StringUtils.getLevenshteinDistance(cmd, theCmd)

			if (distance <= now)
				continue

			mostSimilarCommand = theCmd
			distance = now
		}

		e.isCancelled = true

		e.player.sendMessage("§cComando §e/$cmd§c não existe! Use §e/ajuda§c para obter ajuda!");
		if (15 > distance) {
			e.player.sendMessage("");
			e.player.sendMessage("§7Mas eu acho que você queria escrever §e/$mostSimilarCommand");
		}
	}
}