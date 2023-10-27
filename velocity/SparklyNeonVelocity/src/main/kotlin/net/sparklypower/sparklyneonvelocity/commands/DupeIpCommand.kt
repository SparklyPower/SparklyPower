package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.title.Title
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.Ban
import net.sparklypower.sparklyneonvelocity.dao.ConnectionLogEntry
import net.sparklypower.sparklyneonvelocity.dao.IpBan
import net.sparklypower.sparklyneonvelocity.dao.User
import net.sparklypower.sparklyneonvelocity.tables.Bans
import net.sparklypower.sparklyneonvelocity.tables.ConnectionLogEntries
import net.sparklypower.sparklyneonvelocity.tables.IpBans
import net.sparklypower.sparklyneonvelocity.tables.PremiumUsers
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

class DupeIpCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val playerName = invocation.arguments().getOrNull(0)
        if (playerName == null) {
            invocation.source().sendMessage("§cVocê precisa inserir o nome do player ou o UUID dele!".fromLegacySectionToTextComponent())
            return
        }

        // Primeiramente vamos pegar o UUID para achar o IP
        val playerUniqueId = try {
            UUID.fromString(playerName)
        } catch (e: IllegalArgumentException) {
            m.punishmentManager.getUniqueId(playerName)
        }

        // Vamos pegar o player
        val playerLastConnection = m.pudding.transactionBlocking {
            ConnectionLogEntry.find { (ConnectionLogEntries.player eq playerUniqueId) or (ConnectionLogEntries.ip eq playerName) }
                .maxByOrNull { it.connectedAt }
        }

        if (playerLastConnection == null) {
            invocation.source().sendMessage("§cNão achei nenhum Player com esse nome!".fromLegacySectionToTextComponent())
            return
        }

        val ip = playerLastConnection.ip

        val ipBan = m.pudding.transactionBlocking {
            IpBan.find {
                (IpBans.ip eq ip) and (IpBans.temporary eq false or (IpBans.temporary eq true and IpBans.expiresAt.greaterEq(
                    System.currentTimeMillis()
                )))
            }.firstOrNull()
        }

        val asn = m.asnManager.getAsnForIP(ip)

        // Caso achar...
        invocation.source().sendMessage("Escaneando ${if (ipBan != null) "§c" else "§f"}$ip §a(${asn?.first}, ${asn?.second?.name})".fromLegacySectionToTextComponent())

        // Agora vamos achar todos os players que tem o mesmo IP
        val connectionLogEntries = m.pudding.transactionBlocking {
            ConnectionLogEntry.find {
                ConnectionLogEntries.ip eq playerLastConnection.ip
            }.toList()
        }

        // And now we are going to try getting all players that also have the same latest IP
        val connectionLogEntriesAndLastIPs = m.pudding.transactionBlocking {
            ConnectionLogEntry.find {
                ConnectionLogEntries.player inList connectionLogEntries.map { it.player }
            }.orderBy(ConnectionLogEntries.connectedAt to SortOrder.DESC)
                .limit(1)
                .toList()
                .filter {
                    it.ip == playerLastConnection.ip
                }
        }

        val onlyMatchingAnyIP = connectionLogEntries.filterNot { entry ->
            connectionLogEntriesAndLastIPs.any {
                it.player == entry.player
            }
        }

        val uniqueIds = connectionLogEntriesAndLastIPs.distinctBy { it.player }.map { it.player }
        val anyIPUniqueIds = onlyMatchingAnyIP.distinctBy { it.player }.map { it.player }

        val matchingLastIPsAccounts = uniqueIds.joinToString(", ", transform = {
            // Está banido?
            val ban = m.pudding.transactionBlocking {
                Ban.find {
                    (Bans.player eq it) and (Bans.temporary eq false or (Bans.temporary eq true and Bans.expiresAt.greaterEq(
                        System.currentTimeMillis()
                    )))
                }.firstOrNull()
            }

            // Se ele estiver banido...
            if (ban != null) {
                val punishedName = m.pudding.transactionBlocking { User.findById(ban.player) }

                return@joinToString "§c${punishedName?.username}"
            }

            // Está online?
            val isOnline = server.getPlayer(it).getOrNull()
            if (isOnline != null && isOnline.isActive) {
                // Sim ele está online
                val onlineName = m.pudding.transactionBlocking { User.findById(it) }

                return@joinToString "§a${onlineName?.username}"
            } else {
                // Ele não está online
                val offlineName = m.pudding.transactionBlocking { User.findById(it) }

                return@joinToString "§7${offlineName?.username}"
            }
        })

        val matchingAnyIPsAccounts = anyIPUniqueIds.joinToString(", ", transform = {
            // Está banido?
            val ban = m.pudding.transactionBlocking {
                Ban.find {
                    (Bans.player eq it) and (Bans.temporary eq false or (Bans.temporary eq true and Bans.expiresAt.greaterEq(
                        System.currentTimeMillis()
                    )))
                }.firstOrNull()
            }

            // Se ele estiver banido...
            if (ban != null) {
                val punishedName = m.pudding.transactionBlocking { User.findById(ban.player) }

                return@joinToString "§c${punishedName?.username}"
            }

            // Está online?
            val isOnline = server.getPlayer(it).getOrNull()
            if (isOnline != null && isOnline.isActive) {
                // Sim ele está online
                val onlineName = m.pudding.transactionBlocking { User.findById(it) }

                return@joinToString "§a${onlineName?.username}"
            } else {
                // Ele não está online
                val offlineName = m.pudding.transactionBlocking { User.findById(it) }

                return@joinToString "§7${offlineName?.username}"
            }
        })

        // Mandar o resultado final
        invocation.source().sendMessage("[§cBanidos§f] [§aOnline§f] [§7Offline§f]".fromLegacySectionToTextComponent())
        invocation.source().sendMessage("§bContas que usaram o mesmo IP na última entrada ao servidor: $matchingLastIPsAccounts".fromLegacySectionToTextComponent())
        if (matchingAnyIPsAccounts.isNotEmpty()) {
            invocation.source().sendMessage("§bContas que usaram o mesmo IP alguma vez na vida: $matchingAnyIPsAccounts".fromLegacySectionToTextComponent())
        }
        invocation.source().sendMessage("§7Para mais informações, use §6/advdupeip".fromLegacySectionToTextComponent())
    }
    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.dupeip")
}