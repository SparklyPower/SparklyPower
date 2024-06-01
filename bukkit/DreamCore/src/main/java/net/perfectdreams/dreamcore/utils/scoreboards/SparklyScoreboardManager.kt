package net.perfectdreams.dreamcore.utils.scoreboards

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.event.PlayerScoreboardCreatedEvent
import net.perfectdreams.dreamcore.event.PlayerScoreboardRemovedEvent
import net.perfectdreams.dreamcore.utils.PhoenixScoreboard
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class SparklyScoreboardManager(val m: DreamCore) {
    internal val scoreboards = ConcurrentHashMap<Player, PhoenixScoreboard>()

    fun getScoreboard(player: Player) = scoreboards[player]

    fun getOrCreateScoreboard(player: Player) = getScoreboard(player) ?: createScoreboard(player)

    fun createScoreboard(player: Player): PhoenixScoreboard {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        val phoenixScoreboard = PhoenixScoreboard(scoreboard)
        scoreboards[player] = phoenixScoreboard
        player.scoreboard = scoreboard

        Bukkit.getPluginManager().callEvent(PlayerScoreboardCreatedEvent(player, phoenixScoreboard))

        return phoenixScoreboard
    }

    fun removeScoreboard(player: Player) {
        scoreboards.remove(player)

        Bukkit.getPluginManager().callEvent(PlayerScoreboardRemovedEvent(player))
    }

    fun resetAllScoreboards() {
        val allScoreboards = scoreboards.keys.toList()
        allScoreboards.forEach { removeScoreboard(it) }
        Bukkit.getOnlinePlayers().forEach {
            createScoreboard(it)
        }
    }
}