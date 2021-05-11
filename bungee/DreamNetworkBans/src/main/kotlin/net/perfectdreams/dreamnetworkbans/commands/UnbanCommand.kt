package net.perfectdreams.dreamnetworkbans.commands

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import net.perfectdreams.dreamnetworkbans.dao.GeoLocalization
import net.perfectdreams.dreamnetworkbans.dao.IpBan
import net.perfectdreams.dreamnetworkbans.tables.Bans
import net.perfectdreams.dreamnetworkbans.tables.ConnectionLogEntries
import net.perfectdreams.dreamnetworkbans.tables.GeoLocalizations
import net.perfectdreams.dreamnetworkbans.tables.IpBans
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UnbanCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("unban"), permission = "dreamnetworkbans.unban") {

	@Subcommand
    fun unban(sender: CommandSender, playerName: String) {
		val (punishedDisplayName, punishedUniqueId, player) = PunishmentManager.getPunishedInfoByString(playerName) ?: run {
			sender.sendMessage("§cEu sei que você tá correndo para desbanir aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".toTextComponent())
			return
		}

		if (punishedUniqueId == null) {
			sender.sendMessage("§cNão conheço o UUID desse cara, sorry!".toTextComponent())
			return
		}

		transaction(Databases.databaseNetwork) {
			Bans.deleteWhere { Bans.player eq punishedUniqueId }
		}

		sender.sendMessage("§b${punishedUniqueId}/${punishedDisplayName}§a foi desbanido com sucesso!".toTextComponent())

		PunishmentManager.sendPunishmentToDiscord(
				false,
				punishedDisplayName ?: "Nome desconhecido",
				punishedUniqueId,
				"Desbanido",
				PunishmentManager.getPunisherName(sender),
				null,
				(sender as? ProxiedPlayer)?.server?.info?.name,
				null
		)

		// E agora desbanir o IP
		val storedIp = transaction(Databases.databaseNetwork) {
			ConnectionLogEntries.select {
				ConnectionLogEntries.player eq punishedUniqueId
			}.orderBy(ConnectionLogEntries.connectedAt, SortOrder.DESC)
					.firstOrNull()
		}

		if (storedIp == null) {
			sender.sendMessage("§cIP de §b${punishedUniqueId}/${punishedDisplayName}§c não foi encontrado então a gente não removeu o ban do IP dele ;w;".toTextComponent())
		} else {
			m.proxy.pluginManager.dispatchCommand(sender, "ipunban ${storedIp[ConnectionLogEntries.ip]}")
		}
	}
}