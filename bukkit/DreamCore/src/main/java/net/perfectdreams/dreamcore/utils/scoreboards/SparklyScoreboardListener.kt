package net.perfectdreams.dreamcore.utils.scoreboards

import net.perfectdreams.dreamcore.DreamCore
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class SparklyScoreboardListener(val m: DreamCore) : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(e: PlayerJoinEvent) {
        m.scoreboardManager.createScoreboard(e.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onQuit(e: PlayerQuitEvent) {
        m.scoreboardManager.removeScoreboard(e.player)
    }
}