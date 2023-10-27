package net.sparklypower.sparklyneonvelocity.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.proxy.Player
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity

class ChatListener(private val m: SparklyNeonVelocity) {
    companion object {
        val commands = listOf(
            "login",
            "logar",
            "registrar",
            "register"
        )
    }

    @Subscribe
    fun onQuit(e: DisconnectEvent) {
        m.loggedInPlayers.remove(e.player.uniqueId)
    }

    @Subscribe
    fun onCommand(event: CommandExecuteEvent) {
        val player = event.commandSource
        if (player is Player) {
            if (m.loggedInPlayers.contains(player.uniqueId))
                return

            if (commands.any { event.command.startsWith(it, true) })
                return

            event.result = CommandExecuteEvent.CommandResult.denied()
        }
    }

    @Subscribe
    fun onChat(event: PlayerChatEvent) {
        if (event.player.uniqueId in m.lockedAdminChat) {
            m.broadcastAdminChatMessage(event.player, event.message)
            event.result = PlayerChatEvent.ChatResult.denied()
        }

        if (event.message.startsWith("/")) {
            if (m.loggedInPlayers.contains(event.player.uniqueId))
                return

            if (commands.any { event.message.startsWith(it, true) })
                return

            event.result = PlayerChatEvent.ChatResult.denied()
        }
    }
}