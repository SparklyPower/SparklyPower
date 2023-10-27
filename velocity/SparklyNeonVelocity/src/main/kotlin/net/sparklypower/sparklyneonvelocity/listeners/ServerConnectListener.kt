package net.sparklypower.sparklyneonvelocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity

class ServerConnectListener(val m: SparklyNeonVelocity) {
    @Subscribe
    fun onServerConnect(e: ServerPreConnectEvent) {
        // If the user is trying to connect to a server that isn't "sparklypower_lobby" and they aren't logged in, cancel the event!
        if (e.originalServer.serverInfo.name != "sparklypower_lobby" && !m.loggedInPlayers.contains(e.player.uniqueId)) {
            e.result = ServerPreConnectEvent.ServerResult.denied()
        }
    }
}