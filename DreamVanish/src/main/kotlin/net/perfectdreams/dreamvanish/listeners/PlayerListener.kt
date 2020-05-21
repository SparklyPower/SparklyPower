package net.perfectdreams.dreamvanish.listeners

import net.perfectdreams.dreamvanish.DreamVanish
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(val m: DreamVanish) : Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        if (!e.player.hasPermission("dreamvanish.bypassvanish")) {
            // Can't bypass the vanish status, so...
            DreamVanishAPI.vanishedPlayers.forEach { vanished ->
                e.player.hidePlayer(m, vanished)
            }
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        DreamVanishAPI.vanishedPlayers.remove(e.player)
        DreamVanishAPI.queroTrabalharPlayers.remove(e.player)
    }
}