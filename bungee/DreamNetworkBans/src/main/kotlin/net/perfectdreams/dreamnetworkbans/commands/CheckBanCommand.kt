package net.perfectdreams.dreamnetworkbans.commands

import net.md_5.bungee.api.CommandSender
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import net.perfectdreams.dreamnetworkbans.dao.Ban
import net.perfectdreams.dreamnetworkbans.dao.Warn
import net.perfectdreams.dreamnetworkbans.tables.Bans
import net.perfectdreams.dreamnetworkbans.tables.Warns
import net.perfectdreams.dreamnetworkbans.utils.prettyBoolean
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

class CheckBanCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("checkban"), permission = "dreamnetworkbans.checkban") {

	@Subcommand
	fun checkBan(sender: CommandSender, playerName: String) {
		val punishedUniqueId = try { UUID.fromString(playerName) } catch (e: IllegalArgumentException) { PunishmentManager.getUniqueId(playerName) }

		sender.sendMessage("§eSobre §b$playerName§e...".toTextComponent())

		transaction(Databases.databaseNetwork) {
			val allBans = Ban.find {
				Bans.player eq punishedUniqueId
			}.sortedByDescending {
				it.punishedAt
			}

			val currentlyActiveBan = allBans.firstOrNull {
				if (it.temporary) it.expiresAt!! > System.currentTimeMillis() else true
			}

			// Estamos fazendo isto dentro de uma transaction!!
			// É bom? Não... mas fazer o que né
			sender.sendMessage("§eBanido? ${(currentlyActiveBan != null).prettyBoolean()}".toTextComponent())
			if (currentlyActiveBan != null) {
				sender.sendMessage("§eMotivo do Ban: ${currentlyActiveBan.reason}".toTextComponent())
				sender.sendMessage("§eQuem baniu? §b${PunishmentManager.getPunisherName(currentlyActiveBan.punishedBy)}".toTextComponent())
				sender.sendMessage("§eTemporário? §b${(currentlyActiveBan.temporary).prettyBoolean()}".toTextComponent())
			}

			if (allBans.isNotEmpty()) {
				sender.sendMessage("§eBans anteriores:".toTextComponent())
				allBans.forEach {
					val instant = Instant.ofEpochMilli(it.punishedAt)
							.atZone(ZoneId.of("America/Sao_Paulo"))
							.toOffsetDateTime()

					val day = instant.dayOfMonth.toString().padStart(2, '0')
					val month = instant.monthValue.toString().padStart(2, '0')
					val year = instant.year.toString()

					val hour = instant.hour.toString().padStart(2, '0')
					val minute = instant.minute.toString().padStart(2, '0')

					sender.sendMessage("§f[$day/$month/$year $hour:$minute] §7${it.reason} por ${PunishmentManager.getPunisherName(it.punishedBy)}".toTextComponent())
				}
			}

			val warns = Warn.find { Warns.player eq punishedUniqueId }.toMutableList()
			val validWarns = warns.filter { System.currentTimeMillis() <= PunishmentManager.WARN_EXPIRATION + it.punishedAt }.sortedBy { it.punishedAt }
			val invalidWarns = warns.filter { PunishmentManager.WARN_EXPIRATION + it.punishedAt <= System.currentTimeMillis() }.sortedBy { it.punishedAt }
			sender.sendMessage("§eNúmero de avisos (${validWarns.size} avisos válidos):".toTextComponent())
			for (invalidWarn in invalidWarns) {
				val instant = Instant.ofEpochMilli(invalidWarn.punishedAt)
						.atZone(ZoneId.of("America/Sao_Paulo"))
						.toOffsetDateTime()

				val day = instant.dayOfMonth.toString().padStart(2, '0')
				val month = instant.monthValue.toString().padStart(2, '0')
				val year = instant.year.toString()

				val hour = instant.hour.toString().padStart(2, '0')
				val minute = instant.minute.toString().padStart(2, '0')

				sender.sendMessage("§f[$day/$month/$year $hour:$minute] §7${invalidWarn.reason} por ${PunishmentManager.getPunisherName(invalidWarn.punishedBy)}".toTextComponent())
			}
			for (validWarn in validWarns) {
				val instant = Instant.ofEpochMilli(validWarn.punishedAt)
						.atZone(ZoneId.of("America/Sao_Paulo"))
						.toOffsetDateTime()

				val day = instant.dayOfMonth.toString().padStart(2, '0')
				val month = instant.monthValue.toString().padStart(2, '0')
				val year = instant.year.toString()

				val hour = instant.hour.toString().padStart(2, '0')
				val minute = instant.minute.toString().padStart(2, '0')

				sender.sendMessage("§f[$day/$month/$year $hour:$minute]  §a${validWarn.reason} por ${PunishmentManager.getPunisherName(validWarn.punishedBy)}".toTextComponent())
			}
		}
	}
}