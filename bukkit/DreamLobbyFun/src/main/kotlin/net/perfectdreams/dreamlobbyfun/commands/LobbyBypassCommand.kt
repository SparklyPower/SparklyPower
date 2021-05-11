package net.perfectdreams.dreamlobbyfun.commands

import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import org.bukkit.entity.Player

class LobbyBypassCommand(val m: DreamLobbyFun) : AbstractCommand("lobbybypass", permission = "dreamlobby.bypass") {
	@Subcommand
	fun bypassLobby(p0: Player) {
		if (m.unlockedPlayers.contains(p0)) {
			m.unlockedPlayers.remove(p0)
			p0.sendMessage("§aVocê agora não está mais burlando as restrições do lobby!")
		} else {
			m.unlockedPlayers.add(p0)
			p0.sendMessage("§aVocê agora está burlando as restrições do lobby!")
		}
	}
}