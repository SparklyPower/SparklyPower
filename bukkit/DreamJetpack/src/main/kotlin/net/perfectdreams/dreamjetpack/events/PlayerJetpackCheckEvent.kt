package net.perfectdreams.dreamjetpack.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Triggered when a player's jetpack status is checked, this is checked when the player is flying AND when they activate the jetpack
 *
 * If the event is cancelled, the player will stop flying or, if they are activating the Jetpack, nothing will happen.
 */
class PlayerJetpackCheckEvent(val player: Player) : Event(), Cancellable {
    companion object {
        private val HANDLERS_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS_LIST
        }
    }

    private var isCancelled = false

    override fun getHandlers(): HandlerList {
        return HANDLERS_LIST
    }

    override fun isCancelled() = isCancelled

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }
}