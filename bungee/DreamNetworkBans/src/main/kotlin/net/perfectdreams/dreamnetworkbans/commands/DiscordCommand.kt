package net.perfectdreams.dreamnetworkbans.commands

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.discord.DiscordMessage
import net.perfectdreams.dreamcorebungee.utils.extensions.toBaseComponent
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.dao.DiscordAccount
import net.perfectdreams.dreamnetworkbans.tables.DiscordAccounts
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction


class DiscordCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("discord")) {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage("§dNosso Discord! https://discord.gg/JYN6g2s".toTextComponent())
	}
	
	@Subcommand(["registrar", "register"])
	fun register(sender: ProxiedPlayer) {
		val account = transaction(Databases.databaseNetwork) {
			DiscordAccount.find { DiscordAccounts.minecraftId eq sender.uniqueId }
					.firstOrNull()
		}

		if (account == null) {
			sender.sendMessage("§cVocê não tem nenhum registro pendente! Use \"-registrar ${sender.name}\" no nosso servidor no Discord para registrar a sua conta!".toTextComponent())
			return
		}

		transaction(Databases.databaseNetwork) {
			account.isConnected = true
		}

		transaction(Databases.databaseNetwork) {
			DiscordAccounts.deleteWhere {
				DiscordAccounts.minecraftId eq sender.uniqueId and (DiscordAccounts.id neq account.id)
			}
		}

		sender.sendMessage("§aConta do Discord foi registrada com sucesso, yay!".toTextComponent())

		m.discordAccountAssociationsWebhook.send("Conta **`${sender.name}`** (`${sender.uniqueId}`) foi associada a conta `${account.discordId}` (<@${account.discordId}>)")
	}

	@Subcommand(["desregistrar", "unregister"])
	fun unregister(sender: ProxiedPlayer) {
		val account = transaction(Databases.databaseNetwork) {
			DiscordAccount.find { DiscordAccounts.minecraftId eq sender.uniqueId and (DiscordAccounts.isConnected eq true) }
					.firstOrNull()
		}

		if (account == null) {
			sender.sendMessage("§cVocê não tem nenhum registro! Use \"-registrar ${sender.name}\" no nosso servidor no Discord para registrar a sua conta!".toTextComponent())
			return
		}

		transaction(Databases.databaseNetwork) {
			account.delete()
		}

		sender.sendMessage("§aConta do Discord foi desregistrada com sucesso, yay!".toTextComponent())

		m.discordAccountAssociationsWebhook.send("Conta **`${sender.name}`** (`${sender.uniqueId}`) foi desassociada da conta `${account.discordId}` (<@${account.discordId}>)")
	}
}