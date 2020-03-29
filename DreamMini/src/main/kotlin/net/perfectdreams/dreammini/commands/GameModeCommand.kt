package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player

class GameModeCommand(val m: DreamMini) : SparklyCommand(arrayOf("gm", "gamemode"), permission = "dreammini.gamemode") {

	@Subcommand
	fun root(sender: Player){
		sender.sendMessage(generateCommandInfo("gm <0/1/2/3 | survival/creative/adventure/spectator>"))
	}

	@Subcommand
	fun gamemode(sender: Player, gameMode: String, playerName: String? = null){
		var player = sender

		val gm: String?

		if(playerName != null){
			player = Bukkit.getPlayer(playerName) ?: run {
				sender.sendMessage("§c$playerName está offline ou não existe!")
				return
			}
		}

		if(gameMode.matches("([0-3]|survival|creative|adventure|spectator)".toRegex())) {

			gm = when(gameMode){
				"0" -> "survival"
				"1" -> "creative"
				"2" -> "adventure"
				"3" -> "spectator"
				else -> gameMode
			}

			player.gameMode = GameMode.valueOf(gm.toUpperCase())

			sender.sendMessage("§aModo de jogo de §b${player.name}§a foi alterado para §9${gm}§a!")
		}else{
			sender.sendMessage("§b$gameMode §cNão é um modo de jogo válido!")
		}
	}
}