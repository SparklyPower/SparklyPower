package net.perfectdreams.dreamcore.event

import net.perfectdreams.dreamcore.utils.PhoenixScoreboard
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerScoreboardCreatedEvent(val player: Player, val phoenixScoreboard: PhoenixScoreboard) : Event() {
    companion object {
        private val HANDLERS_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS_LIST
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS_LIST
    }
}