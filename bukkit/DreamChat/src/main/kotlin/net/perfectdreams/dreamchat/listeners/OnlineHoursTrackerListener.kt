package net.perfectdreams.dreamchat.listeners

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.tables.TrackedOnlineHours
import net.perfectdreams.dreamcore.utils.Databases
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class OnlineHoursTrackerListener(val m: DreamChat) : Listener {
    private val loginTimes = mutableMapOf<Player, Instant>()

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        loginTimes[e.player] = Instant.now()
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val loginTime = loginTimes[e.player] ?: return
        loginTimes.remove(e.player)

        m.launchAsyncThread {
            transaction(Databases.databaseNetwork) {
                TrackedOnlineHours.insert {
                    it[TrackedOnlineHours.player] = e.player.uniqueId
                    it[TrackedOnlineHours.loggedIn] = loginTime
                    it[TrackedOnlineHours.loggedOut] = Instant.now()
                }
            }
        }
    }
}