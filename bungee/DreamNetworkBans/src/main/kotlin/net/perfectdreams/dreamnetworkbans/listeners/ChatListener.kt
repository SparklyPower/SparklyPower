package net.perfectdreams.dreamnetworkbans.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.jsonObject
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.perfectdreams.dreamnetworkbans.commands.AdminChatCommand.Companion.lockedChat
import net.perfectdreams.dreamnetworkbans.commands.AdminChatCommand.Companion.broadcastMessage
import java.time.Duration

class ChatListener : Listener {
    companion object {
        val largeMessages = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .build<String, String>()
    }

    @EventHandler
    fun onChat(e: ChatEvent) {
        val player = e.sender as? ProxiedPlayer ?: return

        if (e.isCommand) {
            largeMessages.getIfPresent(e.message)?.let {
                player.sendMessage(it)
                e.isCancelled = true
            }
            return
        }

        if (player !in lockedChat) return
        e.isCancelled = true
        broadcastMessage(player, e.message)
    }

    @EventHandler
    fun onDisconnect(e: PlayerDisconnectEvent) = e.player.let { if (it in lockedChat) lockedChat.remove(it) }
}