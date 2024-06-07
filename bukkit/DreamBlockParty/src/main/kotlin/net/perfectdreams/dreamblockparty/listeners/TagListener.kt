package net.perfectdreams.dreamblockparty.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamblockparty.DreamBlockParty
import net.perfectdreams.dreamcore.tables.EventVictories
import net.perfectdreams.dreamcore.utils.Databases
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class TagListener(val m: DreamBlockParty) : Listener {
    @EventHandler
    fun onTag(event: ApplyPlayerTagsEvent) {
        val lastWinner = transaction(Databases.databaseNetwork) {
            EventVictories.selectAll()
                .where {
                    EventVictories.event eq "Block Party"
                }
                .orderBy(EventVictories.wonAt, SortOrder.DESC)
                .limit(1)
                .lastOrNull()
                ?.get(EventVictories.user)
        }

        if (event.player.uniqueId == lastWinner) {
            event.tags.add(
                PlayerTag(
                    "§b§lÁ",
                    "§b§lÁguia",
                    listOf("${event.player.name}§7 tem olhos de águia e ganhou o último §bBlock Party§7!"),
                    "/blockparty"
                )
            )
        }
    }
}