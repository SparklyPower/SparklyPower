package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.title.Title
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.*
import net.sparklypower.sparklyneonvelocity.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.jvm.optionals.getOrNull

class GeoIpCommand(private val m: SparklyNeonVelocity) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val playerName = invocation.arguments().getOrNull(0)
        if (playerName == null) {
            invocation.source().sendMessage("§cVocê precisa inserir o nome do player ou o UUID dele!".fromLegacySectionToTextComponent())
            return
        }

        val uniqueId = try { UUID.fromString(playerName) } catch (e: IllegalArgumentException) { m.punishmentManager.getUniqueId(playerName) }

        val geoLoc = m.pudding.transactionBlocking {
            GeoLocalization.find { (GeoLocalizations.ip eq playerName) }.lastOrNull()
        }

        if (geoLoc == null) {
            invocation.source().sendMessage("§cNão existe nenhuma localização salva para \"§e$playerName§c\"!".fromLegacySectionToTextComponent())
            return
        }

        invocation.source().sendMessage("§cConsultando o IP §e${geoLoc.ip}§c...".fromLegacySectionToTextComponent())
        invocation.source().sendMessage("§cPaís: §e${geoLoc.country}".fromLegacySectionToTextComponent())
        invocation.source().sendMessage("§cCidade: §e${geoLoc.region}".fromLegacySectionToTextComponent())
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.geoip")
}