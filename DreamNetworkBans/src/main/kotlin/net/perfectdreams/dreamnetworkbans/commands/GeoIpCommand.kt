package net.perfectdreams.dreamnetworkbans.commands

import net.md_5.bungee.api.CommandSender
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import net.perfectdreams.dreamnetworkbans.dao.GeoLocalization
import net.perfectdreams.dreamnetworkbans.tables.GeoLocalizations
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class GeoIpCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("geoip"), permission = "dreamnetworkbans.geoip") {

    @Subcommand
    fun root(sender: CommandSender) {
        sender.sendMessage("§c/geoip <player>".toTextComponent())
    }

    @Subcommand
    fun geoip(sender: CommandSender, name: String) {
        val uniqueId = try { UUID.fromString(name) } catch (e: IllegalArgumentException) { PunishmentManager.getUniqueId(name) }

        val geoLoc = transaction(Databases.databaseNetwork) {
            GeoLocalization.find { (GeoLocalizations.ip eq name) }.lastOrNull()
        }

        if (geoLoc == null) {
            sender.sendMessage("§cNão existe nenhuma localização salva para \"§e$name§c\"!".toTextComponent())
            return
        }

        sender.sendMessage("§cConsultando o IP §e${geoLoc.ip}§c...".toTextComponent())
        sender.sendMessage("§cPaís: §e${geoLoc.country}".toTextComponent())
        sender.sendMessage("§cCidade: §e${geoLoc.region}".toTextComponent())
    }
}