package net.perfectdreams.dreamfight.handlers

import net.perfectdreams.dreamfight.DreamFight
import net.perfectdreams.dreamfight.utils.FightModifier
import net.perfectdreams.dreamfight.utils.WinReason
import net.perfectdreams.dreamcore.utils.extensions.healAndFeed
import net.perfectdreams.dreamcore.utils.extensions.removeAllPotionEffects
import net.perfectdreams.dreamcorreios.events.CorreiosItemReceivingEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent

class FightListener(val m: DreamFight) : Listener {
    @EventHandler
    fun onCorreiosItemReceive(event: CorreiosItemReceivingEvent) {
        if (event.player in m.fight.players)
            event.result = CorreiosItemReceivingEvent.PlayerInEventResult
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onDamage(e: EntityDamageEvent) {
        if (e.entity.world.name == "EventoFight")
            if (m.fight.isPvPStarted)
                e.isCancelled = e.entity != m.fight.p1 && e.entity != m.fight.p2 && e.entity != m.fight.p3 && e.entity != m.fight.p4
            else
                e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onDamage(e: EntityDamageByEntityEvent) {
        if (e.entity.world.name == "EventoFight") {
            if (e.damager is Player && e.entity is Player) {
                if (m.fight.isPvPStarted && ((e.damager == m.fight.p1 && e.entity == m.fight.p2) || (e.entity == m.fight.p1 && e.damager == m.fight.p2))) {
                    // Yeah they are fighting boiii
                } else {
                    // They are not fighting, sad sad
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        if (m.fight.started) {
            if (!m.fight.modifiers.contains(
                    FightModifier.TWO_TEAM)) {
                if (e.entity == m.fight.p1) {
                    m.fight.setWinner(
                        m.fight.p2, WinReason.DEATH)
                }
                if (e.entity == m.fight.p2) {
                    m.fight.setWinner(
                        m.fight.p1, WinReason.DEATH)
                }
            } else {
                if (m.fight.shouldEndPvP(e.entity)) {
                    m.fight.setWinner()
                }
            }
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        if (m.fight.started) {
            if (m.fight.players.contains(e.player)) {
                if (!m.fight.modifiers.contains(
                        FightModifier.TWO_TEAM)) {
                    if (e.player.equals(m.fight.p1)) {
                        m.fight.setWinner(
                            m.fight.p2,
                            WinReason.DISCONNECT
                        )
                        return
                    }
                    if (e.player.equals(m.fight.p2)) {
                        m.fight.setWinner(
                            m.fight.p1,
                            WinReason.DISCONNECT
                        )
                        return
                    }
                } else {
                    if (m.fight.shouldEndPvP(e.player)) {
                        m.fight.setWinner()
                        return
                    }
                }
                m.fight.clearInventoryWithArmorOf(e.player)
                e.player.removeAllPotionEffects()
                e.player.healAndFeed()
                m.fight.restoreInventoryOf(
                    e.player
                )
                m.fight.players.remove(e.player)
                e.player.teleport(m.fight.exit)
            }
        }
    }
}