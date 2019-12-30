package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class BroadcastCommand(val m: DreamMini) : SparklyCommand(arrayOf("broadcast", "anunciar"), permission = "dreammini.broadcast") {

	@Subcommand
	fun root(sender: CommandSender, arguments: Array<String>) {
		if(arguments.isNotEmpty()){
			Bukkit.broadcastMessage("§8[§c§lAnúncio§8] §a${arguments.joinToString(" ")}")
		}else{
			sender.sendMessage(generateCommandInfo("broadcast <mensagem>"))
		}
	}
}