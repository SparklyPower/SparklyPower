package net.perfectdreams.dreamscoreboard.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamcore.tables.EventVictories
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamscoreboard.DreamScoreboard
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId

class TagListener(val plugin: DreamScoreboard) : Listener {
    @EventHandler
    fun onTag(e: ApplyPlayerTagsEvent) {
        val userCount = EventVictories.user.count()

        val start = Instant.now()
            .atZone(ZoneId.of("America/Sao_Paulo"))
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .toEpochSecond() * 1000

        val end = Instant.now()
            .atZone(ZoneId.of("America/Sao_Paulo"))
            .toEpochSecond() * 1000

        val results = transaction(Databases.databaseNetwork) {
            EventVictories.slice(EventVictories.user, userCount).select {
                EventVictories.wonAt greaterEq start
            }.groupBy(EventVictories.user)
                .orderBy(userCount to SortOrder.DESC)
                .limit(3)
                .toList()
        }

        val top1 = results.getOrNull(0)

        if (top1 != null && top1[EventVictories.user] == e.player.uniqueId) {
            e.tags.add(
                PlayerTag(
                    "§x§f§f§2§1§4§6§lC",
                    "§x§f§f§2§1§4§6§lCampeão",
                    listOf(
                        "§r§b${e.player.displayName}§r§7 é o top vencedor de eventos do §4§lSparkly§b§lPower§r§7",
                        "§7nos últimos 30 dias!",
                        "",
                        "§7Será que você consegue passar? :3 §6/eventos top"
                    ),
                    "/eventos top",
                    true
                )
            )
        }

        val averageWins = plugin.getTopClubeVictoriesOnAverageOnThisMonth()

        val bestClube = averageWins.firstOrNull()

        if (bestClube != null && bestClube.first.id == ClubeAPI.getPlayerClube(e.player)?.id) {
            val clube = bestClube.first

            e.tags.add(
                PlayerTag(
                    "§x§f§f§2§1§4§6§lF",
                    "§x§f§f§2§1§4§6§lFrenéticos",
                    listOf(
                        "§r§b${e.player.displayName}§r§7 está no ${clube.shortName}§7, o clube com mais",
                        "§7vitórias de eventos nos últimos 30 dias!",
                        "",
                        "§7Será que você consegue passar? :3 §6/eventos top clubes"
                    ),
                    "/eventos top clubes",
                    true
                )
            )
        }
    }
}