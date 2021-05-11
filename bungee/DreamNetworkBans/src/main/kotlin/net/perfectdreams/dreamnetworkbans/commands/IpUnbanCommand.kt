package net.perfectdreams.dreamnetworkbans.commands

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.perfectdreams.commands.ArgumentType
import net.perfectdreams.commands.annotation.InjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import net.perfectdreams.dreamnetworkbans.dao.Ban
import net.perfectdreams.dreamnetworkbans.dao.GeoLocalization
import net.perfectdreams.dreamnetworkbans.dao.IpBan
import net.perfectdreams.dreamnetworkbans.tables.ConnectionLogEntries
import net.perfectdreams.dreamnetworkbans.tables.GeoLocalizations
import net.perfectdreams.dreamnetworkbans.tables.IpBans
import net.perfectdreams.dreamnetworkbans.utils.convertToEpochMillisRelativeToNow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class IpUnbanCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("ipunban", "desbanirip", "unbanip", "ipdesbanir"), permission = "dreamnetworkbans.ipunban") {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage("§cUse /ipunban ip".toTextComponent())
	}

	@Subcommand
	fun withoutReason(sender: CommandSender, player: String) {
		ban(sender, player, null)
	}

	@Subcommand
	fun ban(sender: CommandSender, ip: String, @InjectArgument(ArgumentType.ALL_ARGUMENTS) reason: String?) {
		val playerIp = if (!ip.contains(".")) {
			val player = PunishmentManager.getPunishedInfoByString(ip) ?: run {
				sender.sendMessage("§cPlayer $ip não existe!".toTextComponent())
				return
			}

			val playerUniqueId = player.uniqueId ?: run {
				sender.sendMessage("§cPlayer $ip não existe!".toTextComponent())
				return
			}

			val resultRow = transaction(Databases.databaseNetwork) {
				ConnectionLogEntries.select {
					ConnectionLogEntries.player eq playerUniqueId
				}.maxBy { it[ConnectionLogEntries.connectedAt] }
			} ?: run {
				sender.sendMessage("§cO player $ip nunca jogou no servidor!".toTextComponent())
				return
			}

			resultRow[ConnectionLogEntries.ip]
		} else {
			ip
		}

		transaction(Databases.databaseNetwork) {
			IpBans.deleteWhere {
				IpBans.ip eq playerIp
			}
		}

		val punisherDisplayName = PunishmentManager.getPunisherName(sender)

		sender.sendMessage("§b${playerIp}§a foi desbanido com sucesso, yay!! ^-^".toTextComponent())

		val hiddenIp = PunishmentManager.hideIp(playerIp)
		PunishmentManager.sendPunishmentToDiscord(
				false,
				hiddenIp,
				null,
				"IP Unban",
				punisherDisplayName,
				null,
				(sender as? ProxiedPlayer)?.server?.info?.name,
				null
		)

		m.proxy.broadcast("§b${punisherDisplayName}§a desbaniu §c${hiddenIp}§a!".toTextComponent())
	}
}