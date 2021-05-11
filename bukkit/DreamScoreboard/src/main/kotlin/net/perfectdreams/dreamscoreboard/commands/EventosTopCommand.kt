package net.perfectdreams.dreamscoreboard.commands

import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.tables.EventVictories
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamscoreboard.DreamScoreboard
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object EventosTopCommand : DSLCommandBase<DreamScoreboard> {
    override fun command(plugin: DreamScoreboard) = create(listOf("eventos top")) {
        executes {
            sender.sendMessage("§8[ §bTop Vencedores de Evento do Mês §8]".centralizeHeader())
            // sender.sendMessage("§eVeja os clubes que venceram mais eventos neste mês em §6/eventos top clubes")
            // sender.sendMessage("§7")

            async {
                val start = plugin.getMonthStartInMillis()

                val end = Instant.now()
                    .atZone(ZoneId.of("America/Sao_Paulo"))
                    .toEpochSecond() * 1000

                val userCount = EventVictories.user.count()

                val results = transaction(Databases.databaseNetwork) {
                    EventVictories.slice(EventVictories.user, userCount).select {
                        EventVictories.wonAt greaterEq start
                    }.groupBy(EventVictories.user)
                        .orderBy(userCount to SortOrder.DESC)
                        .limit(10)
                        .toList()
                }

                val selfResults = transaction(Databases.databaseNetwork) {
                    EventVictories.select {
                        EventVictories.wonAt greaterEq start and (EventVictories.user eq player.uniqueId)
                    }.count()
                }

                player.sendMessage("§eVocê ganhou §a${selfResults} eventos§e neste mês")
                player.sendMessage("")
                for ((index, result) in results.take(10).withIndex()) {
                    val uuid = result[EventVictories.user]
                    val count = result[userCount]

                    player.sendMessage("§e${index + 1}. §b${transaction(Databases.databaseNetwork) { User.findById(uuid)?.username} }§e com §a${count} vitórias")
                    if (index == 0) {
                        player.sendMessage("§8> §c§lGANHA QUATRO PESADELOS A CADA HORA ONLINE!")
                    }
                    if (index == 1) {
                        player.sendMessage("§8> §c§lGANHA DOIS PESADELOS A CADA HORA ONLINE!")
                    }
                    if (index == 2) {
                        player.sendMessage("§8> §c§lGANHA UM PESADELO A CADA HORA ONLINE!")
                    }
                }
            }
        }
    }
}