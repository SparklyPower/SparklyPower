package net.sparklypower.sparklyneonvelocity.commands

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.title.Title
import net.sparklypower.common.utils.fromLegacySectionToTextComponent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import net.sparklypower.sparklyneonvelocity.dao.ConnectionLogEntry
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

class ConnectionLogCommand(val m: SparklyNeonVelocity) : SimpleCommand {
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

        // Agora vamos achar todos os players que tem o mesmo IP ou todos os IPs que o player utilizou
        val connections = m.pudding.transactionBlocking {
            // Mas se estamos procurando pelo PLAYER, queremos saber das alts dele!
            // Para isso, vamos pegar todas as conexões de cada IP que o usuário já usou!
            ConnectionLogEntry.find { ConnectionLogEntries.player eq playerUniqueId }.sortedBy { it.connectedAt }.toList()
        }

        if (connections.isEmpty()) {
            invocation.source().sendMessage("§cO player $playerName nunca jogou no servidor!".fromLegacySectionToTextComponent())
            return
        }

        // Caso achar...
        invocation.source().sendMessage("§7Escaneando §b$playerName".fromLegacySectionToTextComponent())

        var currentIp = ""

        val retrievedNames = mutableMapOf<UUID, String>()

        for (connection in connections) {
            if (currentIp != connection.ip) {
                currentIp = connection.ip
                invocation.source().sendMessage("§7Lista de jogadores que utilizaram §b$currentIp§7...".fromLegacySectionToTextComponent())
            }

            val instant = Instant.ofEpochMilli(connection.connectedAt)
            val instantAtZone = instant.atZone(ZoneId.systemDefault())
            val hour = instantAtZone.hour.toString().padStart(2, '0')
            val minute = instantAtZone.minute.toString().padStart(2, '0')
            val second = instantAtZone.second.toString().padStart(2, '0')

            val day = instantAtZone.dayOfMonth.toString().padStart(2, '0')
            val month = instantAtZone.monthValue.toString().padStart(2, '0')
            val year = instantAtZone.year

            val playerNameFromUniqueId = retrievedNames.getOrPut(connection.player) {
                m.pudding.transactionBlocking { User.findById(connection.player) }?.username
                    ?: connection.player.toString()
            }

            invocation.source().sendMessage(
                "§8• ${connection.connectionStatus.color}${playerNameFromUniqueId} §7às §f$hour:$minute:$second $day/$month/$year".fromLegacySectionToTextComponent().apply {
                    this.hoverEvent(HoverEvent.showText("§eStatus: §6${connection.connectionStatus.color}${connection.connectionStatus.fancyName}\n§eUUID: §6${connection.player}\n§7Tentou se conectar às $hour:$minute:$second $day/$month/$year".fromLegacySectionToTextComponent()))
                }
            )
        }
    }
    override fun hasPermission(invocation: SimpleCommand.Invocation) = invocation.source().hasPermission("sparklyneonvelocity.connectionlog")
}