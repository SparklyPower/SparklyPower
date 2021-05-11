package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

class HatCommand(val m: DreamMini) : SparklyCommand(arrayOf("hat", "capacete", "chapéu"), permission = "dreammini.hat"){

	@Subcommand
	fun root(sender: Player, playerName: String? = null){
		var player = sender

		if(playerName != null){
			player = Bukkit.getPlayer(playerName) ?: run {
				sender.sendMessage("§c$playerName está offline ou não existe!")
				return
			}
		}

		val item = player.inventory.itemInMainHand

		if(item != null) {
			val type = item.type

			player.inventory.setItemInMainHand(player.inventory.helmet)

			player.inventory.helmet = item

			if(type != Material.AIR){
				sender.sendMessage("§a(ﾉ ≧ ∀ ≦)ﾉ Adorei seu novo look!")
			}else{
				sender.sendMessage("§aVocê consegue novamente sentir o vento soprar sua cabeça! ヽ(･ˇ ∀ˇ･ゞ)")
			}
		}else{
			sender.sendMessage("§cSegure um item/bloco na sua mão antes de usar!")
		}
	}
}