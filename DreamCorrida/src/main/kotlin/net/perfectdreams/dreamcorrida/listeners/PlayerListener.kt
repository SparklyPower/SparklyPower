package net.perfectdreams.dreamcorrida.listeners

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.PlayerUtils
import net.perfectdreams.dreamcore.utils.balance
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcorrida.DreamCorrida
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(val m: DreamCorrida) : Listener {
    @EventHandler
    fun onElytra(e: EntityToggleGlideEvent) {
        if (!m.eventoCorrida.running)
            return

        val eventoCorrida = m.eventoCorrida
        val corrida = eventoCorrida.corrida ?: return
        val spawnLocation = corrida.spawn.toLocation()

        if (spawnLocation.world.name != e.entity.world.name)
            return

        e.isCancelled = true
    }

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (!m.eventoCorrida.running)
            return

        if (!e.displaced)
            return

        val eventoCorrida = m.eventoCorrida
        val corrida = eventoCorrida.corrida ?: return
        val spawnLocation = corrida.spawn.toLocation()

        if (spawnLocation.world.name != e.player.world.name)
            return

        val currentPlayerCheckpoint = eventoCorrida.playerCheckpoints[e.player]
        val currentCheckpointIndex = corrida.checkpoints.indexOf(currentPlayerCheckpoint)

        for ((index, checkpoint) in corrida.checkpoints.withIndex()) {
            if (e.player.location.isWithinRegion(checkpoint.regionName)) {
                if (index > currentCheckpointIndex) {
                    e.player.playSound(e.player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)

                    if (index + 1 == corrida.checkpoints.size) {
                        if (eventoCorrida.wonPlayers.size == 0)
                            m.lastWinner = e.player.uniqueId

                        // Player venceu a corrida!
                        eventoCorrida.wonPlayers.add(e.player.uniqueId)
                        val howMuchMoneyWillBeGiven = 15_000 / eventoCorrida.wonPlayers.size
                        val howMuchNightmaresWillBeGiven = 1

                        e.player.balance += howMuchMoneyWillBeGiven
                        scheduler().schedule(m, SynchronizationContext.ASYNC) {
                            Cash.giveCash(e.player, howMuchNightmaresWillBeGiven.toLong())
                        }

                        e.player.fallDistance = 0.0f
                        e.player.fireTicks = 0
                        PlayerUtils.healAndFeed(e.player)

                        e.player.teleport(DreamCore.dreamConfig.spawn)

                        Bukkit.broadcastMessage("${DreamCorrida.PREFIX} §b${e.player.displayName}§a venceu a corrida em ${eventoCorrida.wonPlayers.size}º lugar! Ele ganhou §2$howMuchMoneyWillBeGiven sonhos§a e §c$howMuchNightmaresWillBeGiven pesadelo§a!")

                        if (eventoCorrida.wonPlayers.size == 5) { // Finalizar corrida
                            Bukkit.broadcastMessage("${DreamCorrida.PREFIX} §eEvento Corrida acabou, obrigado a todos que participaram! ^-^")

                            spawnLocation.world.players.forEach {
                                it.fallDistance = 0.0f
                                it.fireTicks = 0
                                PlayerUtils.healAndFeed(it)

                                it.teleport(DreamCore.dreamConfig.spawn)
                            }

                            eventoCorrida.running = false
                            eventoCorrida.playerCheckpoints.clear()
                            eventoCorrida.lastTime = System.currentTimeMillis()
                            eventoCorrida.wonPlayers.clear()
                        }
                        return
                    }

                    e.player.sendTitle("§bCheckpoint §e${index + 1}§b/§e${corrida.checkpoints.size}", "§3${checkpoint.fancyName}", 10, 20, 10)

                    eventoCorrida.playerCheckpoints[e.player] = checkpoint
                    return
                }
            }
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        m.eventoCorrida.playerCheckpoints.remove(e.player)

        val isWithinACorridaWorld = m.availableCorridas.any { it.spawn.world == e.player.world.name }

        if (isWithinACorridaWorld) {
            e.player.teleport(DreamCore.dreamConfig.spawn)
        }
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        if (!m.eventoCorrida.running)
            return

        if (e.entity !is Player)
            return

        val player = e.entity as Player
        val eventoCorrida = m.eventoCorrida
        val corrida = eventoCorrida.corrida ?: return
        val spawnLocation = corrida.spawn.toLocation() ?: return

        if (spawnLocation.world.name != player.world.name)
            return

        e.isCancelled = true

        if (e.cause == EntityDamageEvent.DamageCause.FALL
            || e.cause == EntityDamageEvent.DamageCause.CONTACT
            || e.cause == EntityDamageEvent.DamageCause.CRAMMING
            || e.cause == EntityDamageEvent.DamageCause.CUSTOM
            || e.cause == EntityDamageEvent.DamageCause.FALLING_BLOCK
            || e.cause == EntityDamageEvent.DamageCause.SUFFOCATION
            || e.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
            || e.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
            || e.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
            || e.cause == EntityDamageEvent.DamageCause.DROWNING)
            return

        // Mas se for qualquer outra coisa...
        val currentPlayerCheckpoint = eventoCorrida.playerCheckpoints[player]
        val teleportTo = currentPlayerCheckpoint?.spawn?.toLocation() ?: corrida.spawn.toLocation()

        player.fallDistance = 0.0f
        player.fireTicks = 0
        PlayerUtils.healAndFeed(player)

        player.teleport(teleportTo)
        player.sendMessage("§cVocê levou dano e voltou ao último checkpoint! Mas não desista, continue correndo!")
    }
}