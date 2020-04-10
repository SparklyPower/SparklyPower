package net.perfectdreams.dreamlabirinto.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamlabirinto.DreamLabirinto
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TagListener(val m: DreamLabirinto) : Listener {
    @EventHandler
    fun onApplyTag(e: ApplyPlayerTagsEvent) {
        if (e.player.uniqueId == m.config.winner) {
            e.tags.add(
                PlayerTag(
                    "§a§lA",
                    "§a§lAventureiro",
                    listOf(
                        "§r§b${e.player.displayName}§r§7 foi persistente e conseguiu ser o primeiro a vencer o evento labirinto!"
                    ),
                    null,
                    false
                )
            )
        }
    }
}