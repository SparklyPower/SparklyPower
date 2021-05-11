package net.perfectdreams.dreamnetworkbans.commands

import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.commands.annotation.Subcommand
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.HoverEvent
import java.lang.IllegalArgumentException
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import java.util.UUID
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import org.jetbrains.exposed.sql.transactions.transaction
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamnetworkbans.dao.GeoLocalization
import net.perfectdreams.dreamnetworkbans.tables.GeoLocalizations
import net.perfectdreams.dreamcorebungee.dao.User
import net.perfectdreams.dreamcorebungee.utils.extensions.toBaseComponent
import net.perfectdreams.dreamnetworkbans.dao.Ban
import net.perfectdreams.dreamnetworkbans.dao.ConnectionLogEntry
import net.perfectdreams.dreamnetworkbans.tables.Bans
import net.perfectdreams.dreamnetworkbans.tables.ConnectionLogEntries
import org.jetbrains.exposed.sql.or
import java.time.Instant
import java.time.ZoneId

class AdvDupeIpCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("advancedupeip", "advdupeip"), permission = "dreamnetworkbans.advdupeip") {
	@Subcommand
	fun advDupeIp(sender: CommandSender, playerName: String) {
		// Primeiramente vamos pegar o UUID para achar o IP
		val playerUniqueId = try {
			UUID.fromString(playerName)
		} catch (e: IllegalArgumentException) {
			PunishmentManager.getUniqueId(playerName)
		}

		val lookUpIp = playerName.contains(".")

		// Agora vamos achar todos os players que tem o mesmo IP ou todos os IPs que o player utilizou
		val connections = transaction(Databases.databaseNetwork) {
			if (lookUpIp) {
				ConnectionLogEntry.find { ConnectionLogEntries.ip eq playerName }.sortedBy { it.connectedAt }.toList()
			} else {
				val tempConnections = ConnectionLogEntry.find { ConnectionLogEntries.player eq playerUniqueId }

				// Mas se estamos procurando pelo PLAYER, queremos saber das alts dele!
				// Para isso, vamos pegar todas as conexões de cada IP que o usuário já usou!
				ConnectionLogEntry.find { ConnectionLogEntries.ip inList tempConnections.map { it.ip }.distinct() }.sortedBy { it.connectedAt }.toList()
			}
		}

		if (connections.isEmpty()) {
			if (lookUpIp) {
				sender.sendMessage("§cO IP $playerName nunca jogou no servidor!".toTextComponent())
			} else {
				sender.sendMessage("§cO player $playerName nunca jogou no servidor!".toTextComponent())
			}
			return
		}

		// Caso achar...
		sender.sendMessage("§7Escaneando §b$playerName".toTextComponent())

		var currentIp = ""

		val retrievedNames = mutableMapOf<UUID, String>()

		for (connection in connections) {
			if (currentIp != connection.ip) {
				currentIp = connection.ip
				sender.sendMessage("§7Lista de jogadores que utilizaram §b$currentIp§7...".toTextComponent())
			}

			val instant = Instant.ofEpochMilli(connection.connectedAt)
			val instantAtZone = instant.atZone(ZoneId.systemDefault())
			val hour = instantAtZone.hour.toString().padStart(2, '0')
			val minute = instantAtZone.minute.toString().padStart(2, '0')
			val second = instantAtZone.second.toString().padStart(2, '0')

			val day = instantAtZone.dayOfMonth.toString().padStart(2, '0')
			val month = instantAtZone.monthValue.toString().padStart(2, '0')
			val year = instantAtZone.year

			val playerNameFromUniqueId = retrievedNames.getOrPut(connection.player, {
				transaction(Databases.databaseNetwork) { User.findById(connection.player) }?.username
						?: connection.player.toString()
			})

			sender.sendMessage(
					"§8• ${connection.connectionStatus.color}${playerNameFromUniqueId} §7às §f$hour:$minute:$second $day/$month/$year".toTextComponent().apply {
						this.hoverEvent = HoverEvent(
								HoverEvent.Action.SHOW_TEXT,
								"§eStatus: §6${connection.connectionStatus.color}${connection.connectionStatus.fancyName}\n§eUUID: §6${connection.player}\n§7Tentou se conectar às $hour:$minute:$second $day/$month/$year".toBaseComponent()
						)
					}
			)
		}
	}
}