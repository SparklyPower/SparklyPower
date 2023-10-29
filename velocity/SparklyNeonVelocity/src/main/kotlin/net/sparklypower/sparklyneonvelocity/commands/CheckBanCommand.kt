package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.PunishmentManager
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.Ban
import net.sparklypower.sparklyneonvelocity.dao.Warn
import net.sparklypower.sparklyneonvelocity.tables.Bans
import net.sparklypower.sparklyneonvelocity.tables.Warns
import net.sparklypower.sparklyneonvelocity.utils.prettyBoolean
import java.time.Instant
import java.time.ZoneId
import java.util.*

class CheckBanCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val playerName = invocation.arguments().getOrNull(0)
        if (playerName == null) {
            invocation.source().sendMessage("§cUse /checkban jogador".fromLegacySectionToTextComponent())
            return
        }

        val punishedUniqueId = try { UUID.fromString(playerName) } catch (e: IllegalArgumentException) { m.punishmentManager.getUniqueId(playerName) }

        invocation.source().sendMessage("§eSobre §b$playerName§e...".fromLegacySectionToTextComponent())

        m.pudding.transactionBlocking {
            val allBans = Ban.find {
                Bans.player eq punishedUniqueId
            }.sortedByDescending {
                it.punishedAt
            }

            val currentlyActiveBan = allBans.firstOrNull {
                if (it.temporary) it.expiresAt!! > System.currentTimeMillis() else true
            }

            // Estamos fazendo isto dentro de uma transaction!!
            // É bom? Não... mas fazer o que né
            invocation.source().sendMessage("§eBanido? ${(currentlyActiveBan != null).prettyBoolean()}".fromLegacySectionToTextComponent())
            if (currentlyActiveBan != null) {
                invocation.source().sendMessage("§eMotivo do Ban: ${currentlyActiveBan.reason}".fromLegacySectionToTextComponent())
                invocation.source().sendMessage("§eQuem baniu? §b${m.punishmentManager.getPunisherName(currentlyActiveBan.punishedBy)}".fromLegacySectionToTextComponent())
                invocation.source().sendMessage("§eTemporário? §b${(currentlyActiveBan.temporary).prettyBoolean()}".fromLegacySectionToTextComponent())
            }

            if (allBans.isNotEmpty()) {
                invocation.source().sendMessage("§eBans anteriores:".fromLegacySectionToTextComponent())
                allBans.forEach {
                    val instant = Instant.ofEpochMilli(it.punishedAt)
                        .atZone(ZoneId.of("America/Sao_Paulo"))
                        .toOffsetDateTime()

                    val day = instant.dayOfMonth.toString().padStart(2, '0')
                    val month = instant.monthValue.toString().padStart(2, '0')
                    val year = instant.year.toString()

                    val hour = instant.hour.toString().padStart(2, '0')
                    val minute = instant.minute.toString().padStart(2, '0')

                    invocation.source().sendMessage("§f[$day/$month/$year $hour:$minute] §7${it.reason} por ${m.punishmentManager.getPunisherName(it.punishedBy)}".fromLegacySectionToTextComponent())
                }
            }

            val warns = Warn.find { Warns.player eq punishedUniqueId }.toMutableList()
            val validWarns = warns.filter { System.currentTimeMillis() <= PunishmentManager.WARN_EXPIRATION + it.punishedAt }.sortedBy { it.punishedAt }
            val invalidWarns = warns.filter { PunishmentManager.WARN_EXPIRATION + it.punishedAt <= System.currentTimeMillis() }.sortedBy { it.punishedAt }
            invocation.source().sendMessage("§eNúmero de avisos (${validWarns.size} avisos válidos):".fromLegacySectionToTextComponent())
            for (invalidWarn in invalidWarns) {
                val instant = Instant.ofEpochMilli(invalidWarn.punishedAt)
                    .atZone(ZoneId.of("America/Sao_Paulo"))
                    .toOffsetDateTime()

                val day = instant.dayOfMonth.toString().padStart(2, '0')
                val month = instant.monthValue.toString().padStart(2, '0')
                val year = instant.year.toString()

                val hour = instant.hour.toString().padStart(2, '0')
                val minute = instant.minute.toString().padStart(2, '0')

                invocation.source().sendMessage("§f[$day/$month/$year $hour:$minute] §7${invalidWarn.reason} por ${m.punishmentManager.getPunisherName(invalidWarn.punishedBy)}".fromLegacySectionToTextComponent())
            }
            for (validWarn in validWarns) {
                val instant = Instant.ofEpochMilli(validWarn.punishedAt)
                    .atZone(ZoneId.of("America/Sao_Paulo"))
                    .toOffsetDateTime()

                val day = instant.dayOfMonth.toString().padStart(2, '0')
                val month = instant.monthValue.toString().padStart(2, '0')
                val year = instant.year.toString()

                val hour = instant.hour.toString().padStart(2, '0')
                val minute = instant.minute.toString().padStart(2, '0')

                invocation.source().sendMessage("§f[$day/$month/$year $hour:$minute]  §a${validWarn.reason} por ${m.punishmentManager.getPunisherName(validWarn.punishedBy)}".fromLegacySectionToTextComponent())
            }
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.checkban")
}