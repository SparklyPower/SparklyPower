package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.sparklypower.common.utils.convertToEpochMillisRelativeToNow
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.PunishmentManager
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.IpBan
import java.util.*
import kotlin.jvm.optionals.getOrNull

class IpBanCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val ip = invocation.arguments().getOrNull(0)
        if (ip == null) {
            invocation.source().sendMessage("§cUse /ipban ip motivo".fromLegacySectionToTextComponent())
            return
        }
        val reason = invocation.arguments().drop(1).joinToString(" ").ifEmpty { null }

        var effectiveReason = reason ?: "Sem motivo definido"

        var silent = false
        if (effectiveReason.contains("-s")) {
            silent = true

            effectiveReason = effectiveReason.replace("-s", "")
        }

        var temporary = false
        var time = 0.toLong()
        if (effectiveReason.contains("-t")) {
            temporary = true

            val splitted = effectiveReason.split("-t")
            val timeSpec = splitted[1]

            val timeMillis = timeSpec.convertToEpochMillisRelativeToNow()
            if (timeMillis <= System.currentTimeMillis()) { // :rolling_eyes:
                invocation.source().sendMessage("§cNão sei se você está congelado no passado, mas o tempo que você passou está no passado! o.O".fromLegacySectionToTextComponent())
                return
            }

            effectiveReason = effectiveReason.replace("-t$timeSpec", "")
            time = timeMillis
        }

        val punisherDisplayName = m.punishmentManager.getPunisherName(invocation.source())

        m.pudding.transactionBlocking {
            IpBan.new {
                this.ip = ip

                this.punishedBy = (invocation.source() as? Player)?.uniqueId
                this.punishedAt = System.currentTimeMillis()
                this.reason = effectiveReason

                if (temporary) {
                    this.temporary = true
                    this.expiresAt = time
                }
            }
        }

        val playersWithThatIp = server.allPlayers.filter {
            it.remoteAddress.address.hostAddress == ip
        }.forEach {
            // Vamos expulsar o player ao ser IP ban
            it.disconnect("""
			§cVocê foi banido!
			§cMotivo:

			§a$effectiveReason
			§cPor: $punisherDisplayName
        """.trimIndent().fromLegacySectionToTextComponent())
        }

        invocation.source().sendMessage("§b${ip}§a foi punido com sucesso, yay!! ^-^".fromLegacySectionToTextComponent())

        val hiddenIp = m.punishmentManager.hideIp(ip)
        m.punishmentManager.sendPunishmentToDiscord(
            silent,
            m.punishmentManager.hideIp(ip),
            null,
            "IP Ban ${if (temporary) "Temporariamente" else "Permanentemente"}",
            punisherDisplayName,
            effectiveReason,
            (invocation.source() as? Player)?.currentServer?.getOrNull()?.server?.serverInfo?.name,
            if (temporary) time else null
        )

        if (!silent) {
            server.sendMessage("§b${punisherDisplayName}§a baniu §c${hiddenIp}§a por §6\"§e${effectiveReason}§6\"§a!".fromLegacySectionToTextComponent())
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.ipban")
}