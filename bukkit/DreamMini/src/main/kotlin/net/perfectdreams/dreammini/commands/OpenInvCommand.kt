package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class OpenInvCommand(val m: DreamMini) : SparklyCommand(arrayOf("openinv", "openinventory", "seeinv"), permission = "dreammini.openinventory"){
	@Subcommand
	fun root(sender: Player, playerName: String? = null){
		val player = Bukkit.getPlayer(playerName ?: return) ?: run {
			sender.sendMessage("§c$playerName está offline ou não existe!")
			return
		}

		sender.openInventory(player.inventory)
	}
}