package net.perfectdreams.dreamcore.utils.scoreboards

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.event.PlayerScoreboardCreatedEvent
import net.perfectdreams.dreamcore.event.PlayerScoreboardRemovedEvent
import net.perfectdreams.dreamcore.utils.PhoenixScoreboard
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R2.scoreboard.CraftScoreboard
import org.bukkit.craftbukkit.v1_20_R2.scoreboard.CraftScoreboardManager
import org.bukkit.craftbukkit.v1_20_R2.util.WeakCollection
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import java.util.concurrent.ConcurrentHashMap

class SparklyScoreboardManager(val m: DreamCore) {
    companion object {
        // 13/07/2021 - YES, THIS IS NEEDED, PAPER HASN'T FIXED THIS ISSUE YET
        // THERE IS A PATCH THAT IMPLEMENTS SCOREBOARD CLEAN UP, BUT THAT DOESN'T FIX OUR USE CASE!!
        // https://canary.discord.com/channels/@me/646488209274044457/864264697356091393
        private val scoreboardsField by lazy {
            (Bukkit.getScoreboardManager() as CraftScoreboardManager)::class.java.getDeclaredField("scoreboards").apply {
                this.isAccessible = true
            }
        }

        private val playerBoardsField by lazy {
            (Bukkit.getScoreboardManager() as CraftScoreboardManager)::class.java.getDeclaredField("playerBoards")
                .apply {
                    this.isAccessible = true
                }
        }

        fun cleanUp(scoreboard: Scoreboard) {
            // Needs to be fixed: https://github.com/PaperMC/Paper/issues/4260
            val scoreboards = scoreboardsField.get(Bukkit.getScoreboardManager()) as WeakCollection<Scoreboard>
            scoreboards.remove(scoreboard)
        }
    }

    internal val scoreboards = ConcurrentHashMap<Player, PhoenixScoreboard>()

    fun startScoreboardCleanUpTask() {
        // Needs to be fixed: https://github.com/PaperMC/Paper/issues/4260
        m.launchMainThread {
            while (true) {
                val weakCollection = scoreboardsField.get(Bukkit.getScoreboardManager()) as WeakCollection<CraftScoreboard>

                m.logger.info("Weak Collection before clean up size is ${weakCollection.size}")

                val playerBoards = playerBoardsField.get(Bukkit.getScoreboardManager()) as Map<CraftPlayer, CraftScoreboard>

                weakCollection.removeAll((weakCollection - playerBoards.values).toSet())

                m.logger.info("Weak Collection after clean up size is ${weakCollection.size}")

                delayTicks(20 * 60) // 1 minute
            }
        }
    }

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
        // Manually clean up scoreboards
        val playerScoreboard = scoreboards[player]
        playerScoreboard?.let {
            cleanUp(it.scoreboard)
        }

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