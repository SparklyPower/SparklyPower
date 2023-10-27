package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.sparklypower.common.utils.convertToEpochMillisRelativeToNow
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.PunishmentManager
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.IpBan
import net.sparklypower.sparklyneonvelocity.tables.ConnectionLogEntries
import net.sparklypower.sparklyneonvelocity.tables.IpBans
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.jvm.optionals.getOrNull

class IpUnbanCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val ip = invocation.arguments().getOrNull(0)
        if (ip == null) {
            invocation.source().sendMessage("§cUse /ipunban ip".fromLegacySectionToTextComponent())
            return
        }
        val playerIp = if (!ip.contains(".")) {
            val player = m.punishmentManager.getPunishedInfoByString(ip) ?: run {
                invocation.source().sendMessage("§cPlayer $ip não existe!".fromLegacySectionToTextComponent())
                return
            }

            val playerUniqueId = player.uniqueId ?: run {
                invocation.source().sendMessage("§cPlayer $ip não existe!".fromLegacySectionToTextComponent())
                return
            }

            val resultRow = m.pudding.transactionBlocking {
                ConnectionLogEntries.select {
                    ConnectionLogEntries.player eq playerUniqueId
                }.maxByOrNull { it[ConnectionLogEntries.connectedAt] }
            } ?: run {
                invocation.source().sendMessage("§cO player $ip nunca jogou no servidor!".fromLegacySectionToTextComponent())
                return
            }

            resultRow[ConnectionLogEntries.ip]
        } else {
            ip
        }

        m.pudding.transactionBlocking {
            IpBans.deleteWhere {
                IpBans.ip eq playerIp
            }
        }

        val punisherDisplayName = m.punishmentManager.getPunisherName(invocation.source())

        invocation.source().sendMessage("§b${playerIp}§a foi desbanido com sucesso, yay!! ^-^".fromLegacySectionToTextComponent())

        val hiddenIp = m.punishmentManager.hideIp(playerIp)
        m.punishmentManager.sendPunishmentToDiscord(
            false,
            hiddenIp,
            null,
            "IP Unban",
            punisherDisplayName,
            null,
            (invocation.source() as? Player)?.currentServer?.getOrNull()?.server?.serverInfo?.name,
            null
        )

        server.sendMessage("§b${punisherDisplayName}§a desbaniu §c${hiddenIp}§a!".fromLegacySectionToTextComponent())
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.ipunban")
}