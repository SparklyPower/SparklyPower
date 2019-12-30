package net.perfectdreams.dreamquiz.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamquiz.DreamQuiz
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TagListener : Listener {

    @EventHandler
    fun onTag(event: ApplyPlayerTagsEvent) {
        if (event.player.uniqueId in DreamQuiz.QUIZ.winners) {
            event.tags.add(
                PlayerTag(
                    "§6§lI",
                    "§6§lInteligente",
                    listOf("§r§e${event.player.name}§7 é inteligente e ganhou o §6Evento Quiz§7 na última vez"),
                    "/quiz"
                )
            )
        }
    }
}