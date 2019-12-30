package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class OpenInvCommand(val m: DreamMini) : SparklyCommand(arrayOf("openinv", "openinventory", "seeinv"), permission = "dreammini.openinventory"){
	@Subcommand
	fun root(sender: Player, playerName: String? = null){
		val player = Bukkit.getPlayer(playerName)

		if (player == null) {
			sender.sendMessage("§c$playerName não existe ou está offline!")
			return
		}

		sender.openInventory(player.inventory)
	}
}