package net.perfectdreams.dreamcore.listeners

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.dao.User
import net.perfectdreams.dreamcore.utils.Databases
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerLoginListener(val m: DreamCore) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onLogin(e: AsyncPlayerPreLoginEvent) {
        val playerName = e.name
        val uniqueId = e.uniqueId

        transaction(Databases.databaseNetwork) {
            User.findById(uniqueId) ?: User.new(uniqueId) {
                m.logger.info("Creating new user data for ($playerName / $uniqueId)")
                this.username = playerName
            }
        }
    }
}