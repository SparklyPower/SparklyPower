package net.perfectdreams.dreamraffle.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.formatted
import net.perfectdreams.dreamraffle.tasks.RafflesManager.lastWinner
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ApplyTagListener : Listener {
    @EventHandler
    fun onApplyTag(event: ApplyPlayerTagsEvent) {
        with (event) {
            lastWinner?.let {
                val color = it.type.colors.default
                val currency = it.type.currency.displayName
                val prize = (it.type.currency.unitaryPrice * it.raffleTickets).formatted

                if (it.uuid == player.uniqueId)
                    tags.add(
                        PlayerTag(
                            "${color}§lS",
                            "${color}§lSortud${player.artigo}",
                            listOf(
                                "§r$color${player.name}§7 venceu a última rifa e",
                                "recebeu $color$prize ${currency}§7."
                            ),
                            "/rifa",
                            false
                        )
                    )
            }
        }
    }
}