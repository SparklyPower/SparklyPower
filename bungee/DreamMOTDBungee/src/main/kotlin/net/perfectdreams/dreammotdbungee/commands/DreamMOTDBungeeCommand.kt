package net.perfectdreams.dreammotdbungee.commands

import net.md_5.bungee.api.CommandSender
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreammotdbungee.DreamMOTDBungee

class DreamMOTDBungeeCommand(val m: DreamMOTDBungee) : SparklyBungeeCommand(arrayOf("dreammotdbungee"), permission = "dreammotdbungee.setup") {
	@Subcommand
	fun root(sender: CommandSender) {
		if (sender.hasPermission("dreammotdbungee.setup")) {
			sender.sendMessage("§aÍcones recarregados!".toTextComponent())

			m.loadFavicons()
		} else {
			sender.sendMessage("§cSem permissão!".toTextComponent())
		}
	}
}