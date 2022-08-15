package net.perfectdreams.dreamchat.listeners

import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.tables.TrackedOnlineHours
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class OnlineHoursTrackerListener(val m: DreamChat) : Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        // Player joined! Let's insert to the database
        m.launchAsyncThread {
            val dbId = transaction(Databases.databaseNetwork) {
                TrackedOnlineHours.insertAndGetId {
                    it[TrackedOnlineHours.player] = e.player.uniqueId
                    it[TrackedOnlineHours.loggedIn] = Instant.now()
                    it[TrackedOnlineHours.loggedOut] = Instant.now()
                }
            }

            onMainThread {
                m.loginTimeDatabaseIds[e.player] = dbId.value
            }
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        // Player left, let's update the player's final time and remove it from our map
        val loginTimeDatabaseId = m.loginTimeDatabaseIds[e.player] ?: return
        m.loginTimeDatabaseIds.remove(e.player)

        m.launchAsyncThread {
            transaction(Databases.databaseNetwork) {
                TrackedOnlineHours.update({ TrackedOnlineHours.id eq loginTimeDatabaseId }) {
                    it[TrackedOnlineHours.loggedOut] = Instant.now()
                }
            }
        }
    }
}