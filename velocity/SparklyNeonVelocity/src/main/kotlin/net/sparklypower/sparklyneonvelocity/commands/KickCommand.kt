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

class KickCommand(private val m: SparklyNeonVelocity) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val playerName = invocation.arguments().getOrNull(0)
        if (playerName == null) {
            invocation.source().sendMessage("§cUse /ban jogador motivo".fromLegacySectionToTextComponent())
            return
        }
        val reason = invocation.arguments().drop(1).joinToString(" ").ifEmpty { null }

        val (punishedDisplayName, punishedUniqueId, player) = m.punishmentManager.getPunishedInfoByString(playerName) ?: run {
            invocation.source().sendMessage("§cEu sei que você tá correndo para expulsar aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".fromLegacySectionToTextComponent())
            return
        }

        if (player == null) {
            invocation.source().sendMessage("§cNós conhecemos $playerName, mas ele não está online!".fromLegacySectionToTextComponent())
            return
        }

        val punisherDisplayName = m.punishmentManager.getPunisherName(invocation.source())

        var effectiveReason = reason ?: "Sem motivo definido"

        var silent = false
        if (effectiveReason.endsWith("-f")) {
            player.disconnect("Internal Exception: java.io.IOException: An existing connection was forcibly closed by the remote host".trimIndent().fromLegacySectionToTextComponent())

            invocation.source().sendMessage("§a${player.username} (${player.uniqueId}) kickado com sucesso pelo motivo \"$effectiveReason\"".fromLegacySectionToTextComponent())
            return
        }
        if (effectiveReason.endsWith("-s")) {
            silent = true

            effectiveReason = effectiveReason.substring(0, (effectiveReason.length - "-s".length) - 1)
        }

        m.punishmentManager.sendPunishmentToDiscord(
            silent,
            punishedDisplayName ?: "Nome desconhecido",
            punishedUniqueId!!,
            "Expulso",
            punisherDisplayName,
            effectiveReason,
            (invocation.source() as? Player)?.currentServer?.getOrNull()?.server?.serverInfo?.name,
            null
        )

        player.disconnect("""
            §cVocê foi expulso do servidor!
            §cMotivo:

            §a$effectiveReason
            §cPor: ${m.punishmentManager.getPunisherName(invocation.source())}
			§7Não se preocupe, você poderá voltar a jogar simplesmente entrando novamente no servidor!
		""".trimIndent().fromLegacySectionToTextComponent())
        invocation.source().sendMessage("§a${player.username} (${player.uniqueId}) kickado com sucesso pelo motivo \"$effectiveReason\"".fromLegacySectionToTextComponent())
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.kick")
}