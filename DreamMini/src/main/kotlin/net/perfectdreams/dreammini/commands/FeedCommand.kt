package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class FeedCommand(val m: DreamMini) : SparklyCommand(arrayOf("fome", "food"), permission = "dreammini.fome"){

	@Subcommand
	fun root(sender: Player, playerName: String? = null){
		var player = sender

		if(playerName != null){

			if(Bukkit.getPlayer(playerName) == null){
				sender.sendMessage("§c$playerName está offline ou não existe!")
				return
			}else{
				player = Bukkit.getPlayer(playerName)
			}
		}

		player.foodLevel = 20
		sender.sendMessage("§b${player.name}§a teve a fome restaurada!")
	}
}