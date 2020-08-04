package net.perfectdreams.dreamloja.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamloja.dao.UserShopVote
import net.perfectdreams.dreamloja.tables.UserShopVotes
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class TagListener : Listener {
    @EventHandler
    fun onTag(e: ApplyPlayerTagsEvent) {
        val votes = transaction(Databases.databaseNetwork) {
            UserShopVote.find {
                UserShopVotes.receivedAt greaterEq (System.currentTimeMillis() - 2_592_000_000) // 30 dias
            }.toMutableList()
        }

        val map = mutableMapOf<UUID, Int>()

        for (vote in votes) {
            val voteCount = map.getOrDefault(vote.receivedBy, 0)
            map[vote.receivedBy] = voteCount + 1
        }

        val bestShops = map.entries.sortedByDescending { it.value }

        val top1 = bestShops.getOrNull(0)
        val top2 = bestShops.getOrNull(1)
        val top3 = bestShops.getOrNull(2)

        if (top1?.key == e.player.uniqueId) {
            e.tags.add(
                PlayerTag(
                    "§x§5§a§f§c§0§3§lC",
                    "§x§5§a§f§c§0§3§lCEO",
                    listOf(
                        "§r§b${e.player.displayName}§r§7 tem a loja mais acessada do §4§lSparkly§b§lPower§r§7",
                        "§7nos últimos 30 dias!",
                        "",
                        "§7Que tal visitar? :3 §6/loja ${e.player.name}"
                    ),
                    "/loja ${e.player.name}",
                    true
                )
            )
        }
        if (top2?.key == e.player.uniqueId) {
            e.tags.add(
                PlayerTag(
                    "§x§5§a§f§c§0§3§lE",
                    "§x§5§a§f§c§0§3§lEmpresário",
                    listOf(
                        "§r§b${e.player.displayName}§r§7 tem a segunda loja mais acessada do §4§lSparkly§b§lPower§r§7",
                        "§7nos últimos 30 dias!",
                        "",
                        "§7Que tal visitar? :3 §6/loja ${e.player.name}"
                    ),
                    "/loja ${e.player.name}",
                    true
                )
            )
        }
        if (top3?.key == e.player.uniqueId) {
            e.tags.add(
                PlayerTag(
                    "§x§5§a§f§c§0§3§lC",
                    "§x§5§a§f§c§0§3§lCamelô",
                    listOf(
                        "§r§b${e.player.displayName}§r§7 tem a terceira loja mais acessada do §4§lSparkly§b§lPower§r§7",
                        "§7nos últimos 30 dias!",
                        "",
                        "§7Que tal visitar? :3 §6/loja ${e.player.name}"
                    ),
                    "/loja ${e.player.name}",
                    true
                )
            )
        }
    }
}