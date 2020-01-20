package net.perfectdreams.dreamcasamentos.listeners

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcasamentos.DreamCasamentos
import net.perfectdreams.dreamcasamentos.DreamCasamentos.Companion.PREFIX
import net.perfectdreams.dreammeninaapi.MeninaAPI
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot

class MarryListener(val m: DreamCasamentos) : Listener {

    @EventHandler
    fun onKiss(e: PlayerInteractEntityEvent) {
        if (e.hand != EquipmentSlot.HAND)
            return

        val scheduler = Bukkit.getScheduler()

        val player = e.player

        if (!player.isSneaking)
            return

        if (e.rightClicked is Player) {
            val rightClicked = e.rightClicked as Player
            scheduler.schedule(m, SynchronizationContext.ASYNC) {
                val marriedPlayer = m.marriedUsers.getOrPut(e.player) { Bukkit.getPlayer(m.getMarriageFor(e.player)?.getPartnerOf(e.player)) }

                switchContext(SynchronizationContext.SYNC)
                if (marriedPlayer != null && marriedPlayer == e.rightClicked) {
                    player.world.spawnParticle(Particle.HEART, player.location.add(0.0, 2.0, 0.0), 1)
                    player.world.spawnParticle(Particle.HEART, rightClicked.location.add(0.0, 2.0, 0.0), 1)

                    player.sendMessage("§dVocê beijou ${MeninaAPI.getArtigo(rightClicked)} ${rightClicked.displayName}§d! §eʕ•ᴥ•ʔ")
                    rightClicked.sendMessage("§d${player.displayName}§d te beijou §eʕ•ᴥ•ʔ")
                }
            }
        }
    }

}