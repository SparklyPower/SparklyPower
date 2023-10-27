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
import net.sparklypower.sparklyneonvelocity.dao.*
import net.sparklypower.sparklyneonvelocity.tables.ConnectionLogEntries
import net.sparklypower.sparklyneonvelocity.tables.PremiumUsers
import net.sparklypower.sparklyneonvelocity.tables.Warns
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.jvm.optionals.getOrNull

class WarnCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val playerName = invocation.arguments().getOrNull(0)
        if (playerName == null) {
            invocation.source().sendMessage("§cUse /ban jogador motivo".fromLegacySectionToTextComponent())
            return
        }
        val reason = invocation.arguments().drop(1).joinToString(" ").ifEmpty { null }

        val (punishedDisplayName, punishedUniqueId, player) = m.punishmentManager.getPunishedInfoByString(playerName) ?: run {
            invocation.source().sendMessage("§cEu sei que você tá correndo para avisar aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".fromLegacySectionToTextComponent())
            return
        }

        if (punishedUniqueId == null) {
            invocation.source().sendMessage("§cNão conheço o UUID desse cara, sorry!".fromLegacySectionToTextComponent())
            return
        }

        var effectiveReason = reason ?: "Sem motivo definido"

        var silent = false
        if (effectiveReason.endsWith("-s")) {
            effectiveReason = effectiveReason.substring(0, effectiveReason.length - "-s".length)

            silent = true
        }

        val punisherDisplayName = m.punishmentManager.getPunisherName(invocation.source())

        val source = invocation.source()
        m.pudding.transactionBlocking {
            Warn.new {
                this.player = punishedUniqueId
                this.punishedBy = if (source is Player) source.uniqueId else null
                this.punishedAt = System.currentTimeMillis()
                this.reason = effectiveReason
            }
        }

        val warns = m.pudding.transactionBlocking {
            Warn.find { Warns.player eq punishedUniqueId and (Warns.punishedAt greaterEq (System.currentTimeMillis() - PunishmentManager.WARN_EXPIRATION ))}.toList()
        }
        val count = Math.min(warns.size, 5)

        val geoLocalization = m.pudding.transactionBlocking {
            ConnectionLogEntry.find { ConnectionLogEntries.player eq punishedUniqueId }.maxByOrNull { it.connectedAt }
        }

        // IP do usuário, caso seja encontrado
        val ip = player?.remoteAddress?.hostString ?: geoLocalization?.ip

        when (count) {
            2 -> {
                val player = server.getPlayer(punishedUniqueId).getOrNull()

                if (player != null) {
                    player.disconnect("§cVocê está chegando ao limite de avisos, cuidado!\n§cTotal de avisos: §e$count".fromLegacySectionToTextComponent())

                    m.punishmentManager.sendPunishmentToDiscord(
                        silent,
                        punishedDisplayName ?: "Nome desconhecido",
                        punishedUniqueId,
                        "Expulso",
                        punisherDisplayName,
                        effectiveReason,
                        (invocation.source() as? Player)?.currentServer?.getOrNull()?.server?.serverInfo?.name,
                        null
                    )
                    // announceKick(player.name, player.uniqueId, sender, effectiveReason, silent)
                }
            }

            3 -> {
                // Ban de 24 horas

                val expires = System.currentTimeMillis() + 86400000 // 24 horas
                m.pudding.transactionBlocking {
                    Ban.new {
                        this.player = punishedUniqueId!!
                        this.punishedBy = (invocation.source() as? Player)?.uniqueId
                        this.punishedAt = System.currentTimeMillis()
                        this.reason = effectiveReason

                        this.temporary = true
                        this.expiresAt = expires
                    }

                    if (ip != null) {
                        IpBan.new {
                            this.ip = ip

                            this.punishedBy = (invocation.source() as? Player)?.uniqueId
                            this.punishedAt = System.currentTimeMillis()
                            this.reason = effectiveReason
                            this.temporary = true
                            this.expiresAt = expires
                        }
                    }
                }

                // TODO: Hard coded, remover depois
                player?.disconnect("""
					§cVocê foi temporariamente banido!
					§cMotivo:
					
					§a$effectiveReason
					§cPor: $punisherDisplayName
					§cExpira em: §a24 horas
				""".trimIndent().fromLegacySectionToTextComponent())

                // announceBan(player?.name ?: punishedDisplayName!!, player?.uniqueId ?: punishedUniqueId!!, sender, effectiveReason, silent, true, expires)
            }

            4 -> {
                // Ban de 7 dias

                val expires = System.currentTimeMillis() + 604800000 // 12 horas
                m.pudding.transactionBlocking {
                    Ban.new {
                        this.player = punishedUniqueId!!
                        this.punishedBy = (invocation.source() as? Player)?.uniqueId
                        this.punishedAt = System.currentTimeMillis()
                        this.reason = effectiveReason

                        this.temporary = true
                        this.expiresAt = expires
                    }

                    if (ip != null) {
                        IpBan.new {
                            this.ip = ip
                            this.punishedBy = (invocation.source() as? Player)?.uniqueId
                            this.punishedAt = System.currentTimeMillis()
                            this.reason = effectiveReason
                            this.temporary = true
                            this.expiresAt = expires
                        }
                    }
                }

                // TODO: Hard coded, remover depois
                player?.disconnect("""
					§cVocê foi temporariamente banido!
					§cMotivo:
					
					§a$effectiveReason
					§cPor: $punisherDisplayName
					§cExpira em: §a7 dias
				""".trimIndent().fromLegacySectionToTextComponent())


                // announceBan(player?.name ?: punishedDisplayName!!, player?.uniqueId ?: punishedUniqueId!!, sender, effectiveReason, silent, true, expires)
            }

            5 -> {
                // Ban permanente

                m.pudding.transactionBlocking {
                    Ban.new {
                        this.player = punishedUniqueId
                        this.punishedBy = (invocation.source() as? Player)?.uniqueId
                        this.punishedAt = System.currentTimeMillis()
                        this.reason = effectiveReason

                        this.temporary = false
                    }

                    if (ip != null) {
                        IpBan.new {
                            this.ip = ip
                            this.punishedBy = (invocation.source() as? Player)?.uniqueId
                            this.punishedAt = System.currentTimeMillis()
                            this.reason = effectiveReason
                            this.temporary = true
                            this.expiresAt = PunishmentManager.DEFAULT_IPBAN_EXPIRATION
                        }
                    }
                }

                player?.disconnect("""
					§cVocê foi banido!
					§cMotivo:
					
					§a$effectiveReason
					§cPor: $punisherDisplayName
				""".trimIndent().fromLegacySectionToTextComponent())

                // announceBan(player?.name ?: punishedDisplayName!!, player?.uniqueId ?: punishedUniqueId!!, sender, effectiveReason, silent, false)
            }
        }

        invocation.source().sendMessage("§b${punishedDisplayName}§a foi punido com sucesso, yay!! ^-^".fromLegacySectionToTextComponent())

        m.punishmentManager.sendPunishmentToDiscord(
            silent,
            punishedDisplayName ?: "Nome desconhecido",
            punishedUniqueId,
            "Avisado",
            m.punishmentManager.getPunisherName(invocation.source()),
            effectiveReason,
            (invocation.source() as? Player)?.currentServer?.getOrNull()?.server?.serverInfo?.name,
            null
        )

        if (!silent) {
            server.sendMessage("§b${m.punishmentManager.getPunisherName(invocation.source())}§a avisou §c$playerName§a por §6\"§e$reason§6\"§a!".fromLegacySectionToTextComponent())
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.warn")
}