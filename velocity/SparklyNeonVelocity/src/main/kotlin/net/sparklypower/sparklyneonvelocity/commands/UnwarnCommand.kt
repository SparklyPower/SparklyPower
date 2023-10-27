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
import net.sparklypower.sparklyneonvelocity.tables.Bans
import net.sparklypower.sparklyneonvelocity.tables.ConnectionLogEntries
import net.sparklypower.sparklyneonvelocity.tables.PremiumUsers
import net.sparklypower.sparklyneonvelocity.tables.Warns
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

class UnwarnCommand(private val m: SparklyNeonVelocity, private val server: ProxyServer) : SimpleCommand {
    override fun execute(invocation: SimpleCommand.Invocation) {
        val playerName = invocation.arguments().getOrNull(0)
        if (playerName == null) {
            invocation.source().sendMessage("§cUse /unwarn jogador".fromLegacySectionToTextComponent())
            return
        }
        val (punishedDisplayName, punishedUniqueId, player) = m.punishmentManager.getPunishedInfoByString(playerName) ?: run {
            invocation.source().sendMessage("§cEu sei que você tá correndo para banir aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".fromLegacySectionToTextComponent())
            return
        }

        if (punishedUniqueId == null) {
            invocation.source().sendMessage("§cNão conheço o UUID desse cara, sorry!".fromLegacySectionToTextComponent())
            return
        }

        val warn = m.pudding.transactionBlocking {
            Warn.find { Warns.player eq punishedUniqueId }.lastOrNull()
        }

        if (warn == null) {
            invocation.source().sendMessage("§cEste jogador não tem nenhum aviso válido!".fromLegacySectionToTextComponent())
            return
        }

        m.pudding.transactionBlocking {
            warn.delete()
        }

        invocation.source().sendMessage("§aAviso removido com sucesso!".fromLegacySectionToTextComponent())

        m.punishmentManager.sendPunishmentToDiscord(
            false,
            punishedDisplayName ?: "Nome desconhecido",
            punishedUniqueId,
            "Retirado um Aviso",
            m.punishmentManager.getPunisherName(invocation.source()),
            null,
            (invocation.source() as? Player)?.currentServer?.getOrNull()?.server?.serverInfo?.name,
            null
        )
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.unwarn")
}