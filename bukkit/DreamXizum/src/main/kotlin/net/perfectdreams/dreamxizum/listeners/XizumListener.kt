package net.perfectdreams.dreamxizum.listeners

import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.utils.WinType
import net.perfectdreams.dreamxizum.utils.XizumInventoryHolder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

class XizumListener(private val m: DreamXizum) : Listener {
    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val player = e.entity

        // Vamos verificar se o tal usuário estava em uma arena
        val arena = m.arenas.firstOrNull { it.player1 == player || it.player2 == player } ?: return

        e.isCancelled = true

        arena.finishArena(player, WinType.KILLED)
        m.checkQueue()
    }

    @EventHandler
    fun onDisconnect(e: PlayerQuitEvent) {
        val player = e.player

        // Vamos verificar se o tal usuário estava em uma arena
        val arena = m.arenas.firstOrNull { it.player1 == player || it.player2 == player } ?: return

        arena.finishArena(e.player, WinType.DISCONNECTED)
        m.checkQueue()
    }

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        // Remove them from the queue if possible
        if (e.player in m.queue) {
            m.queue.remove(e.player)
            e.player.sendMessage("§cVocê saiu da fila do Xizum, pois você se teletransportou!")
            return
        }

        val player1RequestQueue = m.requestQueue.firstOrNull { it.player1 == e.player }
        if (player1RequestQueue != null) {
            m.requestQueue.remove(player1RequestQueue)
            e.player.sendMessage("§cVocê saiu da fila do Xizum, pois você se teletransportou!")
            player1RequestQueue.player2.sendMessage("§cVocê saiu da fila do Xizum, pois o seu amigo se teletransportou!")
            return
        }

        val player2RequestQueue = m.requestQueue.firstOrNull { it.player2 == e.player }
        if (player2RequestQueue != null) {
            m.requestQueue.remove(player2RequestQueue)
            e.player.sendMessage("§cVocê saiu da fila do Xizum, pois você se teletransportou!")
            player2RequestQueue.player1.sendMessage("§cVocê saiu da fila do Xizum, pois o seu amigo se teletransportou!")
            return
        }
    }

    @EventHandler
    fun onMovePreStart(e: PlayerMoveEvent) {
        if (!e.displaced)
            return

        val player = e.player

        // Vamos verificar se o tal usuário estava em uma arena
        val arena = m.arenas.firstOrNull { it.player1 == player || it.player2 == player } ?: return

        if (arena.isCountingDown)
            e.isCancelled = true
    }

    @EventHandler
    fun onInteractPreStart(e: PlayerInteractEvent) {
        val player = e.player

        // Vamos verificar se o tal usuário estava em uma arena
        val arena = m.arenas.firstOrNull { it.player1 == player || it.player2 == player } ?: return

        if (arena.isCountingDown)
            e.isCancelled = true
    }
}