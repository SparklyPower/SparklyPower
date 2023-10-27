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
import net.sparklypower.sparklyneonvelocity.tables.Bans
import net.sparklypower.sparklyneonvelocity.tables.ConnectionLogEntries
import net.sparklypower.sparklyneonvelocity.tables.PremiumUsers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.jvm.optionals.getOrNull

class UnbanCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val playerName = invocation.arguments().getOrNull(0)
        if (playerName == null) {
            invocation.source().sendMessage("§cUse /ban jogador motivo".fromLegacySectionToTextComponent())
            return
        }
        val (punishedDisplayName, punishedUniqueId, player) = m.punishmentManager.getPunishedInfoByString(playerName) ?: run {
            invocation.source().sendMessage("§cEu sei que você tá correndo para desbanir aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".fromLegacySectionToTextComponent())
            return
        }

        if (punishedUniqueId == null) {
            invocation.source().sendMessage("§cNão conheço o UUID desse cara, sorry!".fromLegacySectionToTextComponent())
            return
        }

        m.pudding.transactionBlocking {
            Bans.deleteWhere { Bans.player eq punishedUniqueId }
        }

        invocation.source().sendMessage("§b${punishedUniqueId}/${punishedDisplayName}§a foi desbanido com sucesso!".fromLegacySectionToTextComponent())

        m.punishmentManager.sendPunishmentToDiscord(
            false,
            punishedDisplayName ?: "Nome desconhecido",
            punishedUniqueId,
            "Desbanido",
            m.punishmentManager.getPunisherName(invocation.source()),
            null,
            (invocation.source() as? Player)?.currentServer?.getOrNull()?.server?.serverInfo?.name,
            null
        )

        // E agora desbanir o IP
        val storedIp = m.pudding.transactionBlocking {
            ConnectionLogEntries.select {
                ConnectionLogEntries.player eq punishedUniqueId
            }.orderBy(ConnectionLogEntries.connectedAt, SortOrder.DESC)
                .firstOrNull()
        }

        if (storedIp == null) {
            invocation.source().sendMessage("§cIP de §b${punishedUniqueId}/${punishedDisplayName}§c não foi encontrado então a gente não removeu o ban do IP dele ;w;".fromLegacySectionToTextComponent())
        } else {
            server.commandManager.executeAsync(invocation.source(), "ipunban ${storedIp[ConnectionLogEntries.ip]}")
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.unban")
}