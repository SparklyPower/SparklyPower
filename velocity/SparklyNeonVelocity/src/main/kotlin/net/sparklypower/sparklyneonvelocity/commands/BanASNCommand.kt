package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.title.Title
import net.sparklypower.common.utils.convertToEpochMillisRelativeToNow
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.PunishmentManager
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.Ban
import net.sparklypower.sparklyneonvelocity.dao.ConnectionLogEntry
import net.sparklypower.sparklyneonvelocity.dao.IpBan
import net.sparklypower.sparklyneonvelocity.dao.User
import net.sparklypower.sparklyneonvelocity.tables.BlockedASNs
import net.sparklypower.sparklyneonvelocity.tables.ConnectionLogEntries
import net.sparklypower.sparklyneonvelocity.tables.PremiumUsers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.jvm.optionals.getOrNull

class BanASNCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val asnId = invocation.arguments().getOrNull(0)?.toIntOrNull()
        if (asnId == null) {
            invocation.source().sendMessage("§cUse /banasn asnid comentário".fromLegacySectionToTextComponent())
            return
        }
        val reason = invocation.arguments().drop(1).joinToString(" ").ifEmpty { null }

        val banned = m.pudding.transactionBlocking {
            val count = BlockedASNs.select { BlockedASNs.id eq asnId }.count()
            if (count == 1L)
                return@transactionBlocking false

            BlockedASNs.insert {
                it[BlockedASNs.id] = asnId
                it[BlockedASNs.comment] = reason
                it[BlockedASNs.blockedAt] = Instant.now()
            }

            return@transactionBlocking true
        }

        if (banned) {
            invocation.source().sendMessage("§aASN §b${asnId}§a foi bloqueado com sucesso, yay!! ^-^ Lembrando que você ainda precisa usar §6/checkandkick§a para expulsar qualquer player que esteja conectado com o ASN banido!".fromLegacySectionToTextComponent())
        } else {
            invocation.source().sendMessage("§cASN §b${asnId}§c já está banido!".fromLegacySectionToTextComponent())

        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.banasn")
}