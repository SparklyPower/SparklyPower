package net.perfectdreams.dreamquiz.listeners

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamquiz.DreamQuiz
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.*

class PlayerListener(val m: DreamQuiz) : Listener {
    @EventHandler
    fun onDrop(e: PlayerDropItemEvent) {
        if (e.player.world.name == "Quiz") {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onShoot(e: EntityShootBowEvent) {
        if (e.entity.world.name == "Quiz") {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.player.world.name == "Quiz") {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onSpawn(e: PlayerQuitEvent) {
        if (e.player.world.name == "Quiz") {
            e.player.teleport(DreamCore.dreamConfig.getSpawn())
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