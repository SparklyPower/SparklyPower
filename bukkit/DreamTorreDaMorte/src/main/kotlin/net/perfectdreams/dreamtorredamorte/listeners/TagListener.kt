package net.perfectdreams.dreamtorredamorte.listeners

import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamtorredamorte.DreamTorreDaMorte
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

class TagListener(val m: DreamTorreDaMorte) : Listener {
    @EventHandler
    fun onTag(event: ApplyPlayerTagsEvent) {
        if (event.player.uniqueId == m.torreDaMorte.lastWinner) {
            event.tags.add(
                PlayerTag(
                    "§4§lV",
                    "§4§lVieirinha",
                    listOf("§r§e${event.player.name}§7 é o Vieirinha e ganhou a última §6Torre da Morte§7!"),
                    "/torre"
                )
            )
        }
    }
}