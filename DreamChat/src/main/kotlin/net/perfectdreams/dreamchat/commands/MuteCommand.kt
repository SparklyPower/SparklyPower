package net.perfectdreams.dreamchat.commands

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import org.bukkit.command.CommandSender

class MuteCommand : AbstractCommand("mute", listOf("mutar", "silenciar"), "dreamchat.mute") {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage(generateCommandInfo("mute", mapOf("nickname" to "Nome do usuário")))
	}

	@Subcommand
	fun mute(sender: CommandSender, username: String) {
		if (DreamChat.mutedUsers.contains(username)) {
			DreamChat.mutedUsers.remove(username)
			sender.sendMessage("§aUsuário §a§n$username§a magicamente aprendeu a falar novamente!")
		} else {
			DreamChat.mutedUsers.add(username)
			sender.sendMessage("§aUsuário §a§n$username§a foi silenciado com sucesso!")
		}
	}
}