package net.perfectdreams.dreamnetworkbans.commands

import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.commands.annotation.Subcommand
import net.md_5.bungee.api.CommandSender
import java.lang.IllegalArgumentException
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import java.util.UUID
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import org.jetbrains.exposed.sql.transactions.transaction
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.dao.User
import net.perfectdreams.dreamnetworkbans.dao.Ban
import net.perfectdreams.dreamnetworkbans.dao.ConnectionLogEntry
import net.perfectdreams.dreamnetworkbans.dao.IpBan
import net.perfectdreams.dreamnetworkbans.tables.Bans
import net.perfectdreams.dreamnetworkbans.tables.ConnectionLogEntries
import net.perfectdreams.dreamnetworkbans.tables.IpBans
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

class DupeIpCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("dupeip"), permission = "dreamnetworkbans.dupeip") {

	@Subcommand
	fun dupeIp(sender: CommandSender, playerName: String) {
		// Primeiramente vamos pegar o UUID para achar o IP
		val playerUniqueId = try {
			UUID.fromString(playerName)
		} catch (e: IllegalArgumentException) {
			PunishmentManager.getUniqueId(playerName)
		}

		// Vamos pegar o player
		val playerLastConnection = transaction(Databases.databaseNetwork) {
			ConnectionLogEntry.find { (ConnectionLogEntries.player eq playerUniqueId) or (ConnectionLogEntries.ip eq playerName) }
				.maxByOrNull { it.connectedAt }
		}

		if (playerLastConnection == null) {
			sender.sendMessage("§cNão achei nenhum Player com esse nome!".toTextComponent())
			return
		}

		val ip = playerLastConnection.ip

		val ipBan = transaction(Databases.databaseNetwork) {
			IpBan.find {
				(IpBans.ip eq ip) and (IpBans.temporary eq false or (IpBans.temporary eq true and IpBans.expiresAt.greaterEq(
					System.currentTimeMillis()
				)))
			}.firstOrNull()
		}

		val asn = m.asnManager.getAsnForIP(ip)

		// Caso achar...
		sender.sendMessage("Escaneando ${if (ipBan != null) "§c" else "§f"}$ip §a(${asn?.first}, ${asn?.second?.name})".toTextComponent())

		// Agora vamos achar todos os players que tem o mesmo IP
		val connectionLogEntries = transaction(Databases.databaseNetwork) {
			ConnectionLogEntry.find {
				ConnectionLogEntries.ip eq playerLastConnection.ip
			}.toList()
		}

		// And now we are going to try getting all players that also have the same latest IP
		val connectionLogEntriesAndLastIPs = transaction(Databases.databaseNetwork) {
			ConnectionLogEntry.find {
				ConnectionLogEntries.player inList connectionLogEntries.map { it.player }
			}.orderBy(ConnectionLogEntries.connectedAt to SortOrder.DESC)
				.limit(1)
				.toList()
				.filter {
					it.ip == playerLastConnection.ip
				}
		}

		val onlyMatchingAnyIP = connectionLogEntries.filterNot { entry ->
			connectionLogEntriesAndLastIPs.any {
				it.player == entry.player
			}
		}

		val uniqueIds = connectionLogEntriesAndLastIPs.distinctBy { it.player }.map { it.player }
		val anyIPUniqueIds = onlyMatchingAnyIP.distinctBy { it.player }.map { it.player }

		val matchingLastIPsAccounts = uniqueIds.joinToString(", ", transform = {
			// Está banido?
			val ban = transaction(Databases.databaseNetwork) {
				Ban.find {
					(Bans.player eq it) and (Bans.temporary eq false or (Bans.temporary eq true and Bans.expiresAt.greaterEq(
						System.currentTimeMillis()
					)))
				}.firstOrNull()
			}

			// Se ele estiver banido...
			if (ban != null) {
				val punishedName = transaction(Databases.databaseNetwork) { User.findById(ban.player) }

				return@joinToString "§c${punishedName?.username}"
			}

			// Está online?
			val isOnline = m.proxy.getPlayer(it)
			if (isOnline != null && isOnline.isConnected) {
				// Sim ele está online
				val onlineName = transaction(Databases.databaseNetwork) { User.findById(it) }

				return@joinToString "§a${onlineName?.username}"
			} else {
				// Ele não está online
				val offlineName = transaction(Databases.databaseNetwork) { User.findById(it) }

				return@joinToString "§7${offlineName?.username}"
			}
		})

		val matchingAnyIPsAccounts = anyIPUniqueIds.joinToString(", ", transform = {
			// Está banido?
			val ban = transaction(Databases.databaseNetwork) {
				Ban.find {
					(Bans.player eq it) and (Bans.temporary eq false or (Bans.temporary eq true and Bans.expiresAt.greaterEq(
						System.currentTimeMillis()
					)))
				}.firstOrNull()
			}

			// Se ele estiver banido...
			if (ban != null) {
				val punishedName = transaction(Databases.databaseNetwork) { User.findById(ban.player) }

				return@joinToString "§c${punishedName?.username}"
			}

			// Está online?
			val isOnline = m.proxy.getPlayer(it)
			if (isOnline != null && isOnline.isConnected) {
				// Sim ele está online
				val onlineName = transaction(Databases.databaseNetwork) { User.findById(it) }

				return@joinToString "§a${onlineName?.username}"
			} else {
				// Ele não está online
				val offlineName = transaction(Databases.databaseNetwork) { User.findById(it) }

				return@joinToString "§7${offlineName?.username}"
			}
		})

		// Mandar o resultado final
		sender.sendMessage("[§cBanidos§f] [§aOnline§f] [§7Offline§f]".toTextComponent())
		sender.sendMessage("§bContas que usaram o mesmo IP na última entrada ao servidor: $matchingLastIPsAccounts".toTextComponent())
		if (matchingAnyIPsAccounts.isNotEmpty()) {
			sender.sendMessage("§bContas que usaram o mesmo IP alguma vez na vida: $matchingAnyIPsAccounts".toTextComponent())
		}
		sender.sendMessage("§7Para mais informações, use §6/advdupeip".toTextComponent())
	}
}
