package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class SpeedCommand(val m: DreamMini) : SparklyCommand(arrayOf("speed", "velocidade"), permission = "dreammini.speed") {

	@Subcommand
	fun root(sender: Player){
		sender.sendMessage(generateCommandInfo("speed <velocidade> ?<player> ?<type[fly|walk]>"))
	}

	@Subcommand
	fun speed(sender: Player, velocity: String, playerName: String? = null, setType: String? = null) {
		var user = sender

		val speedLevel = velocity.toFloatOrNull()

		var type = setType

		if(playerName != null){

			if(Bukkit.getPlayer(playerName) == null){
				sender.sendMessage("§c$playerName está offline ou não existe!")
				return
			}else{
				user = Bukkit.getPlayer(playerName)
			}
		}

		if(type == null){
			type = if(user.isFlying) { "fly" } else { "walk" }
		}

		if(speedLevel != null) {

			val speed = speedLevel / 5

			if (speed in 0.1..1.0) {

				if (type == "fly") {
					user.flySpeed = speed
					sender.sendMessage("§aVelocidade de vôo de §b${user.name}§a foi alterada para §9$speedLevel§a!")
				} else {
					user.walkSpeed = speed
					sender.sendMessage("§aVelocidade no chão de §b${user.name}§a foi alterada para §9$speedLevel§a!")
				}
			} else {
				sender.sendMessage("§cVelocidade precisa estar entre 0 e 5!")
			}
		}
	}
}