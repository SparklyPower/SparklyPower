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
import org.apache.commons.net.util.SubnetUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.jvm.optionals.getOrNull

class IpUnbanCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val rawIp = invocation.arguments().getOrNull(0)
        if (rawIp == null) {
            invocation.source().sendMessage("§cUse /ipunban ip".fromLegacySectionToTextComponent())
            return
        }

        val ipsToBeUnbanned = if (!rawIp.contains(".")) {
            val player = m.punishmentManager.getPunishedInfoByString(rawIp) ?: run {
                invocation.source().sendMessage("§cPlayer $rawIp não existe!".fromLegacySectionToTextComponent())
                return
            }

            val playerUniqueId = player.uniqueId ?: run {
                invocation.source().sendMessage("§cPlayer $rawIp não existe!".fromLegacySectionToTextComponent())
                return
            }

            val resultRow = m.pudding.transactionBlocking {
                ConnectionLogEntries.select {
                    ConnectionLogEntries.player eq playerUniqueId
                }.maxByOrNull { it[ConnectionLogEntries.connectedAt] }
            } ?: run {
                invocation.source().sendMessage("§cO player $rawIp nunca jogou no servidor!".fromLegacySectionToTextComponent())
                return
            }

            listOf(resultRow[ConnectionLogEntries.ip])
        } else {
            try {
                SubnetUtils(rawIp).getInfo().getAllAddresses()
            } catch (e: IllegalArgumentException) {
                listOf(rawIp)
            }
        }

        m.pudding.transactionBlocking {
            IpBans.deleteWhere {
                IpBans.ip inList ipsToBeUnbanned
            }
        }

        val punisherDisplayName = m.punishmentManager.getPunisherName(invocation.source())

        for (playerIp in ipsToBeUnbanned) {
            invocation.source()
                .sendMessage("§b${playerIp}§a foi desbanido com sucesso, yay!! ^-^".fromLegacySectionToTextComponent())

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
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.ipunban")
}