package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.sparklypower.common.utils.convertToEpochMillisRelativeToNow
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.PunishmentManager
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.GeoLocalization
import net.sparklypower.sparklyneonvelocity.dao.IpBan
import net.sparklypower.sparklyneonvelocity.tables.GeoLocalizations
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.jvm.optionals.getOrNull

class IpReportCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        invocation.source().sendMessage("§cResumo dos players que estão online (${server.allPlayers.size}):".fromLegacySectionToTextComponent())
        invocation.source().sendMessage(server.allPlayers.sortedBy { it.remoteAddress.hostString }.joinToString("\n") {
            val geoLocalization = m.pudding.transactionBlocking {
                GeoLocalization.find { GeoLocalizations.ip eq it.remoteAddress.hostString }.firstOrNull()
            }

            val asn = m.asnManager.getAsnForIP(it.remoteAddress.hostString)
            "§b${it.username} §7(§a${it.remoteAddress.hostString} §7- §a${geoLocalization?.country ?: "???"}, ${geoLocalization?.region ?: "???"} §7- §a${asn?.first}, ${asn?.second?.name}§6)"
        }.fromLegacySectionToTextComponent())
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.ipreport")
}