package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class TpAllCommand(val m: DreamMini) : SparklyCommand(arrayOf("tpall"), permission = "dreammini.tpall"){

	@Subcommand
	fun root(sender: Player, playerName: String? = null){
		var player = sender

		if(playerName != null){
			player = Bukkit.getPlayer(playerName) ?: run {
				sender.sendMessage("§c$playerName está offline ou não existe!")
				return
			}
		}

		val player1 = player
		Bukkit.getOnlinePlayers().forEach{ it.teleport(player1.location)}

		sender.sendMessage("§aTodos os players foram teletransportados para §b${player.name}§a!")
	}
}