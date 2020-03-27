package net.perfectdreams.dreamcorrida.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamcorrida.DreamCorrida
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TagListener(val m: DreamCorrida) : Listener {
    @EventHandler
    fun onApplyTag(e: ApplyPlayerTagsEvent) {
        if (e.player.uniqueId == m.lastWinner) {
            e.tags.add(
                PlayerTag(
                    "§3§lC",
                    "§3§lCorredor",
                    listOf(
                        "§r§b${e.player.displayName}§r§7 correu e venceu vários obstáculos e conseguiu ser o primeiro a vencer o evento corrida!"
                    ),
                    null,
                    false
                )
            )
        }
    }
}