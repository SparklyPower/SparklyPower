package net.perfectdreams.dreamantiafk.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent
import java.util.*

class FishListener : Listener {
    // TODO: Move to another plugin
    // TODO: Clean up, this will cause a memory leak because we never clean up fish events!
    val fishEvents = mutableMapOf<UUID, MutableList<FishEvent>>()

    @EventHandler
    fun onFish(event: PlayerFishEvent) {
        val playerFishEvents = fishEvents.getOrPut(event.player.uniqueId) { mutableListOf() }

        playerFishEvents.add(
            FishEvent(
                event.state,
                System.currentTimeMillis()
            )
        )
    }

    data class FishEvent(
        val state: PlayerFishEvent.State,
        val time: Long
    )
}