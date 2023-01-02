package net.perfectdreams.dreamsplegg.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamsplegg.DreamSplegg
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TagListener(val m: DreamSplegg) : Listener {
    @EventHandler
    fun onTag(event: ApplyPlayerTagsEvent) {
        if (event.player.uniqueId == m.splegg.lastWinner) {
            event.tags.add(
                PlayerTag(
                    "§4§lO",
                    "§4§lOvo",
                    listOf("§r§eVocê destruiu o meu ovo!", "${event.player.name}§7 sobreviveu todos os buracos e ganhou o último §6Splegg§7!"),
                    "/splegg"
                )
            )
        }
    }
}