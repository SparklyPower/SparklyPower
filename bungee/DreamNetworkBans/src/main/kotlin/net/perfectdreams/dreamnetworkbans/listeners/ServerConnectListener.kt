package net.perfectdreams.dreamnetworkbans.listeners

import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans

class ServerConnectListener(val m: DreamNetworkBans) : Listener {
    @EventHandler
    fun onServerConnect(e: ServerConnectEvent) {
        // If the user is trying to connect to a server that isn't "sparklypower_lobby" and they aren't logged in, cancel the event!
        if (e.target.name != "sparklypower_lobby" && !m.loggedInPlayers.contains(e.player.uniqueId)) {
            e.isCancelled = true
        }
    }
}