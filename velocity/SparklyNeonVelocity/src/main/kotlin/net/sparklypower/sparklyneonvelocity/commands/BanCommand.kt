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

class BanCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val playerName = invocation.arguments().getOrNull(0)
        if (playerName == null) {
            invocation.source().sendMessage("§cUse /ban jogador motivo".fromLegacySectionToTextComponent())
            return
        }
        val reason = invocation.arguments().drop(1).joinToString(" ").ifEmpty { null }

        val (punishedDisplayName, punishedUniqueId, player) = m.punishmentManager.getPunishedInfoByString(playerName) ?: run {
            invocation.source().sendMessage("§cEu sei que você tá correndo para banir aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".fromLegacySectionToTextComponent())
            return
        }

        var effectiveReason = reason ?: "Sem motivo definido"

        var silent = false
        if (effectiveReason.contains("-s")) {
            silent = true

            effectiveReason = effectiveReason.replace("-s", "")
        }

        var ipBan = false
        if (effectiveReason.contains("-i")) {
            ipBan = true

            effectiveReason = effectiveReason.replace("-i", "")
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

        val geoLocalization = m.pudding.transactionBlocking {
            ConnectionLogEntry.find { ConnectionLogEntries.player eq punishedUniqueId!! }.maxByOrNull { it.connectedAt }
        }

        val ip = if (player != null)
            player.remoteAddress.hostString
        else
            geoLocalization?.ip

        m.pudding.transactionBlocking {
            if (ipBan) {
                if (ip == null) {
                    invocation.source().sendMessage("§cInfelizmente não há nenhum registro de IP do player §e$punishedDisplayName§c!".fromLegacySectionToTextComponent())
                    return@transactionBlocking
                }

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

            Ban.new {
                this.player = punishedUniqueId!!
                this.punishedBy = (invocation.source() as? Player)?.uniqueId
                this.punishedAt = System.currentTimeMillis()
                this.reason = effectiveReason

                if (temporary) {
                    this.temporary = true
                    this.expiresAt = time
                }
            }
        }

        if (ip != null && !ipBan && !temporary) {
            m.pudding.transactionBlocking {
                IpBan.new {
                    this.ip = ip

                    this.punishedBy = (invocation.source() as? Player)?.uniqueId
                    this.punishedAt = System.currentTimeMillis()
                    this.reason = effectiveReason
                    this.temporary = true
                    this.expiresAt = System.currentTimeMillis() + PunishmentManager.DEFAULT_IPBAN_EXPIRATION
                }
            }
        }

        // Vamos expulsar o player ao ser banido
        player?.disconnect("""
			§cVocê foi banido!
			§cMotivo:

			§a$effectiveReason
			§cPor: $punisherDisplayName
        """.trimIndent().fromLegacySectionToTextComponent())

        invocation.source().sendMessage("§b${punishedDisplayName}§a foi punido com sucesso, yay!! ^-^".fromLegacySectionToTextComponent())

        m.punishmentManager.sendPunishmentToDiscord(
            silent,
            punishedDisplayName ?: "Nome desconhecido",
            punishedUniqueId!!,
            "Banido ${if (temporary) "Temporariamente" else "Permanentemente"}",
            punisherDisplayName,
            effectiveReason,
            (invocation.source() as? Player)?.currentServer?.getOrNull()?.server?.serverInfo?.name,
            null
        )

        if (!silent) {
            server.sendMessage("§b${punisherDisplayName}§a baniu §c${punishedDisplayName}§a por §6\"§e${effectiveReason}§6\"§a!".fromLegacySectionToTextComponent())
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.ban")
}