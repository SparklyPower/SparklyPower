package net.perfectdreams.dreamfight.handlers

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamfight.DreamFight
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TagListener : Listener {
    @EventHandler
    fun onTag(event: ApplyPlayerTagsEvent) {
        if (event.player.name == DreamFight.lastWinner)
            event.tags.add(
                PlayerTag(
                    "§4§lL",
                    "§4§lLutador",
                    listOf(
                        "§r§e${event.player.name}§7 venceu todos os seus oponentes em uma batalha arriscada e",
                        "§7ganhou o último §4§lEvento Fight§7!"
                    ),
                    "/fight"
                )
            )
    }
}