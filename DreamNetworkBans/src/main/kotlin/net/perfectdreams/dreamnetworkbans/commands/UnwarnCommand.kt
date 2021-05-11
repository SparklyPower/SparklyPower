package net.perfectdreams.dreamnetworkbans.commands

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import net.perfectdreams.dreamnetworkbans.dao.Warn
import net.perfectdreams.dreamnetworkbans.tables.Warns
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UnwarnCommand : SparklyBungeeCommand(arrayOf("unwarn"), permission = "dreamnetworkbans.unwarn") {
	
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage("§cUse /unwarn usuário".toTextComponent())
	}
	
	@Subcommand
	fun unwarn(sender: CommandSender, playerName: String) {
		val (punishedDisplayName, punishedUniqueId, player) = PunishmentManager.getPunishedInfoByString(playerName) ?: run {
			sender.sendMessage("§cEu sei que você tá correndo para banir aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".toTextComponent())
			return
		}

		if (punishedUniqueId == null) {
			sender.sendMessage("§cNão conheço o UUID desse cara, sorry!".toTextComponent())
			return
		}

		val warn = transaction(Databases.databaseNetwork) {
			Warn.find { Warns.player eq punishedUniqueId }.lastOrNull()
		}
		
		if (warn == null) {
			sender.sendMessage("§cEste jogador não tem nenhum aviso válido!".toTextComponent())
			return
		}
		
		transaction(Databases.databaseNetwork) {
			warn.delete()
		}

		sender.sendMessage("§aAviso removido com sucesso!".toTextComponent())

		PunishmentManager.sendPunishmentToDiscord(
				false,
				punishedDisplayName ?: "Nome desconhecido",
				punishedUniqueId,
				"Retirado um Aviso",
				PunishmentManager.getPunisherName(sender),
				null,
				(sender as? ProxiedPlayer)?.server?.info?.name,
				null
		)
	}
}