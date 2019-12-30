package net.perfectdreams.dreamchat.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamchat.DreamChat
import org.bukkit.command.CommandSender

class DreamChatCommand(val m: DreamChat) : SparklyCommand(arrayOf("dreamchat"), permission = "dreamchat.setup") {
	@Subcommand
	fun root(p0: CommandSender) {
		p0.sendMessage("§6/dreamchat start")
	}

	@Subcommand(["start"])
	fun argument(p0: CommandSender) {
		p0.sendMessage("Iniciando o evento chat...")
		m.eventoChat.preStart()
	}

	@Subcommand(["reload"])
	fun reload(p0: CommandSender) {
		m.reload()

		p0.sendMessage("Reload concluido!")
	}
}