package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class FlyCommand(val m: DreamMini) : SparklyCommand(arrayOf("fly", "voar"), permission = "dreammini.fly"){

	@Subcommand
	fun root(sender: Player, playerName: String? = null){
		var player = sender

		if(playerName != null){
			if(Bukkit.getPlayer(playerName) == null){
				sender.sendMessage("§c$playerName não existe ou está offline!")
				return
			}else{
				player = Bukkit.getPlayer(playerName)
			}
		}

		if(!player.allowFlight){
			player.allowFlight = true
			player.isFlying = true

			sender.sendMessage("§aModo de vôo ativado para §b${player.name}§a, (∩*´∀` )⊃━☆ﾟ.*･｡ﾟ whoosh!")
		}else{
			player.allowFlight = false
			sender.sendMessage("§aModo de vôo desativado para §b${player.name}§a, ( ∩╹□╹∩ ) whoopsie!")
		}
	}
}