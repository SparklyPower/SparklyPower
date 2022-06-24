package net.perfectdreams.dreamtntrun.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamtntrun.DreamTNTRun
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TagListener(val m: DreamTNTRun) : Listener {
    @EventHandler
    fun onTag(event: ApplyPlayerTagsEvent) {
        if (event.player.uniqueId == m.TNTRun.lastWinner) {
            event.tags.add(
                PlayerTag(
                    "§4§lS",
                    "§4§lSobrevivente",
                    listOf("§r§e${event.player.name}§7 sobreviveu todos os buracos e ganhou o último §6TNT Run§7!"),
                    "/tntrun"
                )
            )
        }
    }
}