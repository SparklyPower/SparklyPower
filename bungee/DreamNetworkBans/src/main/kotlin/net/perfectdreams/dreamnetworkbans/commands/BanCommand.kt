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
import net.perfectdreams.dreamnetworkbans.dao.ConnectionLogEntry
import net.perfectdreams.dreamnetworkbans.dao.GeoLocalization
import net.perfectdreams.dreamnetworkbans.dao.IpBan
import net.perfectdreams.dreamnetworkbans.tables.ConnectionLogEntries
import net.perfectdreams.dreamnetworkbans.tables.GeoLocalizations
import net.perfectdreams.dreamnetworkbans.utils.convertToEpochMillisRelativeToNow
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class BanCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("ban"), permission = "dreamnetworkbans.ban") {

	@Subcommand
	fun root(sender: CommandSender) {
		sender.sendMessage("§cUse /ban jogador motivo".toTextComponent())
	}
	
	@Subcommand
	fun withoutReason(sender: CommandSender, player: String) {
		ban(sender, player, null)
	}

	@Subcommand
	fun ban(sender: CommandSender, playerName: String, @InjectArgument(ArgumentType.ALL_ARGUMENTS) reason: String?) {
		val (punishedDisplayName, punishedUniqueId, player) = PunishmentManager.getPunishedInfoByString(playerName) ?: run {
			sender.sendMessage("§cEu sei que você tá correndo para banir aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".toTextComponent())
			return
		}

		var effectiveReason = reason ?: "Sem motivo definido"
		
		var silent = false
		if (effectiveReason.contains("-s")) {
			silent = true
			
			effectiveReason = effectiveReason.replace("-s", "")
		}

		var ipBan = false
		if (effectiveReason.contains("-i")) {
			ipBan = true

			effectiveReason = effectiveReason.replace("-i", "")
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

		val geoLocalization = transaction(Databases.databaseNetwork) {
			ConnectionLogEntry.find { ConnectionLogEntries.player eq punishedUniqueId!! }.maxByOrNull { it.connectedAt }
		}

		val ip = if (player != null)
			player.address.hostString
		else
			geoLocalization?.ip

		transaction(Databases.databaseNetwork) {
			if (ipBan) {
				if (ip == null) {
					sender.sendMessage("§cInfelizmente não há nenhum registro de IP do player §e$punishedDisplayName§c!".toTextComponent())
					return@transaction
				}


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

			Ban.new {
				this.player = punishedUniqueId!!
				this.punishedBy = (sender as? ProxiedPlayer)?.uniqueId
				this.punishedAt = System.currentTimeMillis()
				this.reason = effectiveReason

				if (temporary) {
					this.temporary = true
					this.expiresAt = time
				}
			}
 		}
		
		if (ip != null && !ipBan && !temporary) {
			transaction(Databases.databaseNetwork) {
				IpBan.new {
					this.ip = ip

					this.punishedBy = (sender as? ProxiedPlayer)?.uniqueId
					this.punishedAt = System.currentTimeMillis()
					this.reason = effectiveReason
					this.temporary = true
					this.expiresAt = System.currentTimeMillis() + PunishmentManager.DEFAULT_IPBAN_EXPIRATION
				}
			}
		}

		// Vamos expulsar o player ao ser banido
		player?.disconnect("""
			§cVocê foi banido!
			§cMotivo:

			§a$effectiveReason
			§cPor: $punisherDisplayName
        """.trimIndent().toTextComponent())

		sender.sendMessage("§b${punishedDisplayName}§a foi punido com sucesso, yay!! ^-^".toTextComponent())

		PunishmentManager.sendPunishmentToDiscord(
				silent,
				punishedDisplayName ?: "Nome desconhecido",
				punishedUniqueId!!,
				"Banido ${if (temporary) "Temporariamente" else "Permanentemente"}",
				punisherDisplayName,
				effectiveReason,
				(sender as? ProxiedPlayer)?.server?.info?.name,
				null
		)

		if (!silent) {
			m.proxy.broadcast("§b${punisherDisplayName}§a baniu §c${punishedDisplayName}§a por §6\"§e${effectiveReason}§6\"§a!".toTextComponent())
		}
	}
}