package net.perfectdreams.dreamnetworkbans.commands

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.discord.DiscordMessage
import net.perfectdreams.dreamcorebungee.utils.extensions.toBaseComponent
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans


class AdminChatCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("adminchat", "a"), permission = "dreamnetworkbans.adminchat") {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage("§6/adminchat blah blah blah".toTextComponent())
	}
	
	@Subcommand
	fun adminChat(sender: CommandSender, args: Array<String>) {
		if (args.getOrNull(0) == "bypass") {
			DreamNetworkBans.bypassPremiumCheck = !DreamNetworkBans.bypassPremiumCheck
			sender.sendMessage("Bypass Status: ${DreamNetworkBans.bypassPremiumCheck}")
			return
		}

		val message = args.joinToString(" ")

		val staff = m.proxy.players.filter { it.hasPermission("dreamnetworkbans.adminchat") }

		var senderName = sender.name
		val server = "???"
		var mensagem = "§eServidor: §6$server"

		if (sender is ProxiedPlayer) {
			senderName = sender.displayName
			mensagem = "§eServidor: §6" + sender.server.info.name
		}

		var color = "§7"

		if (sender.hasPermission("dreamnetworkbans.owner")) {
			color = "§a"
		} else if (sender.hasPermission("dreamnetworkbans.admin")) {
			color = "§4"
		} else if (sender.hasPermission("dreamnetworkbans.coord")) {
			color = "§5"
		} else if (sender.hasPermission("dreamnetworkbans.moderator")) {
			color = "§3"
		} else if (sender.hasPermission("dreamnetworkbans.builder")) {
			color = "§5"
		}

		val tc = "§3[$color§l${senderName}§3] §b$message".toTextComponent()
		tc.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, mensagem.toBaseComponent())

		staff.forEach { it.sendMessage(tc) }

		if (staff.size == 1) {
			sender.sendMessage("§cHey... Não sei se você sabe... Mas você está falando sozinho!".toTextComponent())
		}

		m.adminChatWebhook.send(
				WebhookMessageBuilder()
						.setUsername(sender.name)
						.setAvatarUrl("https://sparklypower.net/api/v1/render/avatar?name=${sender.name}&scale=16")
						.setContent(message)
						.build()
		)
	}
}