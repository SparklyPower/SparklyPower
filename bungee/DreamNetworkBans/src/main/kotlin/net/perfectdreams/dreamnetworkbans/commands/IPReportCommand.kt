package net.perfectdreams.dreamnetworkbans.commands

import net.md_5.bungee.api.CommandSender
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.dao.GeoLocalization
import net.perfectdreams.dreamnetworkbans.tables.GeoLocalizations
import org.jetbrains.exposed.sql.transactions.transaction

class IPReportCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("ipreport"), permission = "dreamnetworkbans.ipreport") {
	@Subcommand
	fun ipreport(sender: CommandSender) {
		sender.sendMessage("§cResumo dos players que estão online (${m.proxy.players.size}):".toTextComponent())
		sender.sendMessage(m.proxy.players.sortedBy { it.address.hostString }.joinToString("\n") {
			val geoLocalization = transaction(Databases.databaseNetwork) {
				GeoLocalization.find { GeoLocalizations.ip eq it.address.hostString }.firstOrNull()
			}

			val asn = m.asnManager.getAsnForIP(it.address.hostString)
			"§b${it.name} §7(§a${it.address.hostString} §7- §a${geoLocalization?.country ?: "???"}, ${geoLocalization?.region ?: "???"} §7- §a${asn?.first}, ${asn?.second?.name}§6)"
		}.toTextComponent())
	}
}