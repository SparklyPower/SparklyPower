package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.command.CommandSender

class SudoCommand(val m: DreamMini) : SparklyCommand(arrayOf("sudo"), permission = "dreammini.sudo"){

	@Subcommand
	fun root(sender: Player){
		sender.sendMessage(generateCommandInfo("sudo <player> <[comando|texto]>"))
	}

	@Subcommand
	fun sudo(sender: CommandSender, playerName: String, args: Array<String>){
		val player = Bukkit.getPlayer(playerName)

		if(player == null){
			sender.sendMessage("§b$playerName §cnão existe ou está offline!")
			return
		}

		if (player.hasPermission("dreammini.unsudoable") && !sender.hasPermission("dreamini.overridesudo")) {
			sender.sendMessage("§cVocê não pode usar sudo em §b${player.name}§c!")
			return
		}

		val command = args.toMutableList()
		
		val execute = command.joinToString(" ")

		if (execute.startsWith("c:")) {
			val chat = execute.replaceFirst("c:", "")
			player.chat(chat)
			sender.sendMessage("§b${player.name} §afoi forçado a enviar no chat §e$chat")
		} else {
			player.performCommand(execute)
			sender.sendMessage("§b${player.name} §afoi forçado a usar o comando §e/$execute")
		}
	}
}