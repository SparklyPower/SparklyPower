package net.perfectdreams.dreamquiz.listeners

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamquiz.DreamQuiz
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(val m: DreamQuiz) : Listener {
    @EventHandler
    fun onSpawn(e: PlayerQuitEvent) {
        if (e.player.world.name == "Quiz") {
            e.player.teleport(DreamCore.dreamConfig.spawn)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(e: AsyncPlayerChatEvent) {
        val quiz = DreamQuiz.QUIZ

        if (quiz.started && quiz.players.contains(e.player)) {
            e.isCancelled = true
            e.player.sendMessage("§cParece que existe um poder que te impede de abrir a sua boca no Quiz... É um evento para a mente, e não de ficar tagalerando por aí.")
        }
    }
}