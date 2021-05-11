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
import net.perfectdreams.dreamnetworkbans.tables.GeoLocalizations
import net.perfectdreams.dreamnetworkbans.utils.convertToEpochMillisRelativeToNow
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class IpBanCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("ipban", "banirip"), permission = "dreamnetworkbans.ipban") {
	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage("§cUse /ipban ip motivo".toTextComponent())
	}

	@Subcommand
	fun withoutReason(sender: CommandSender, player: String) {
		ban(sender, player, null)
	}

	@Subcommand
	fun ban(sender: CommandSender, ip: String, @InjectArgument(ArgumentType.ALL_ARGUMENTS) reason: String?) {
		var effectiveReason = reason ?: "Sem motivo definido"

		var silent = false
		if (effectiveReason.contains("-s")) {
			silent = true

			effectiveReason = effectiveReason.replace("-s", "")
		}

		var temporary = false
		var time = 0.toLong()
		if (effectiveReason.contains("-t")) {
			temporary = true

			val splitted = effectiveReason.split("-t")
			val timeSpec = splitted[1]

			val timeMillis = timeSpec.convertToEpochMillisRelativeToNow()
			if (timeMillis <= System.currentTimeMillis()) { // :rolling_eyes:
				sender.sendMessage("§cNão sei se você está congelado no passado, mas o tempo que você passou está no passado! o.O".toTextComponent())
				return
			}

			effectiveReason = effectiveReason.replace("-t$timeSpec", "")
			time = timeMillis
		}

		val punisherDisplayName = PunishmentManager.getPunisherName(sender)

		transaction(Databases.databaseNetwork) {
			IpBan.new {
				this.ip = ip

				this.punishedBy = (sender as? ProxiedPlayer)?.uniqueId
				this.punishedAt = System.currentTimeMillis()
				this.reason = effectiveReason

				if (temporary) {
					this.temporary = true
					this.expiresAt = time
				}
			}
		}

		val playersWithThatIp = m.proxy.players.filter {
			it.address.address.hostAddress == ip
		}.forEach {
			// Vamos expulsar o player ao ser IP ban
			it.disconnect("""
			§cVocê foi banido!
			§cMotivo:

			§a$effectiveReason
			§cPor: $punisherDisplayName
        """.trimIndent().toTextComponent())
		}

		sender.sendMessage("§b${ip}§a foi punido com sucesso, yay!! ^-^".toTextComponent())

		val hiddenIp = PunishmentManager.hideIp(ip)
		PunishmentManager.sendPunishmentToDiscord(
				silent,
				PunishmentManager.hideIp(ip),
				null,
				"IP Ban ${if (temporary) "Temporariamente" else "Permanentemente"}",
				punisherDisplayName,
				effectiveReason,
				(sender as? ProxiedPlayer)?.server?.info?.name,
				if (temporary) time else null
		)

		if (!silent) {
			m.proxy.broadcast("§b${punisherDisplayName}§a baniu §c${hiddenIp}§a por §6\"§e${effectiveReason}§6\"§a!".toTextComponent())
		}
	}
}