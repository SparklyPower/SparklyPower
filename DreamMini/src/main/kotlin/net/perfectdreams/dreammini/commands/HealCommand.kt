package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class HealCommand(val m: DreamMini) : SparklyCommand(arrayOf("heal", "vida", "restaurarvida"), permission = "dreammini.heal"){

	@Subcommand
	fun root(sender: Player, playerName: String? = null){
		var player = sender

		if(playerName != null){
			player = Bukkit.getPlayer(playerName) ?: run {
				sender.sendMessage("§c$playerName está offline ou não existe!")
				return
			}
		}

		player.health = 20.0
		sender.sendMessage("§b${player.name}§a teve a vida restaurada!")
	}
}