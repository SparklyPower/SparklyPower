package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.command.CommandSender

class FacebookCommand(val m: DreamMini) : SparklyCommand(arrayOf("facebook", "fb")) {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage("§aCurta a nossa página no Facebook!§9 https://facebook.com/SparklyPower")
	}
}