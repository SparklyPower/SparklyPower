package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

class HatCommand(val m: DreamMini) : SparklyCommand(arrayOf("hat", "capacete", "chapéu")) {
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

		val type = item.type

		var allowed = false

		if (player.hasPermission("dreammini.hat")) {
			allowed = true
		} else {
			if (type == Material.PAPER) {
				val meta = item.itemMeta
				if (meta.hasCustomModelData()) {
					if (meta.customModelData in 133..169)
						allowed = true
				}
			}
		}

		if (allowed) {
			player.inventory.setItemInMainHand(player.inventory.helmet)

			player.inventory.helmet = item

			if (type != Material.AIR) {
				sender.sendMessage("§a(ﾉ ≧ ∀ ≦)ﾉ Adorei seu novo look!")
			} else {
				sender.sendMessage("§aVocê consegue novamente sentir o vento soprar sua cabeça! ヽ(･ˇ ∀ˇ･ゞ)")
			}
		} else {
			sender.sendMessage("§cVocê não pode colocar este item na sua cabeça!")
		}
	}
}