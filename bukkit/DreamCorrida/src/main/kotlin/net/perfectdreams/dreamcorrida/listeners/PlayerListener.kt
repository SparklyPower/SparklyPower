package net.perfectdreams.dreamcorrida.listeners

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.displaced
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreamcorrida.DreamCorrida
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(val m: DreamCorrida) : Listener {
    @EventHandler
    fun onGoingToCorridaTeleport(e: PlayerChangedWorldEvent) {
        if (!m.eventoCorrida.running)
            return

        val eventoCorrida = m.eventoCorrida
        val corrida = eventoCorrida.corrida ?: return
        val spawnLocation = corrida.spawn.toLocation()

        if (e.from.name != "Corrida" && e.player.world.name == "Corrida")
            e.player.gameMode = GameMode.ADVENTURE
    }

    @EventHandler
    fun onLeavingCorridaTeleport(e: PlayerChangedWorldEvent) {
        if (e.from.name == "Corrida" && e.player.world.name != "Corrida")
            e.player.gameMode = GameMode.SURVIVAL
    }

    @EventHandler
    fun onElytra(e: EntityToggleGlideEvent) {
        val player = (e.entity as? Player) ?: return

        if (!isPlayerInCorrida(player))
            return

        e.isCancelled = true
    }

    @EventHandler
    fun onShoot(e: EntityShootBowEvent) {
        val player = (e.entity as? Player) ?: return

        if (!isPlayerInCorrida(player))
            return

        e.isCancelled = true
    }

    @EventHandler
    fun onMovePreStart(e: PlayerMoveEvent) {
        if (!e.displaced)
            return

        if (!isPlayerInCorrida(e.player))
            return

        if (m.eventoCorrida.startCooldown > 0)
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
                        if (eventoCorrida.wonPlayers.size == 0) {
                            m.lastWinner = e.player.uniqueId
                            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                                DreamCore.INSTANCE.dreamEventManager.addEventVictory(
                                    e.player,
                                    "Corrida"
                                )
                            }
                        }

                        // Player venceu a corrida!
                        eventoCorrida.wonPlayers.add(e.player.uniqueId)
                        val howMuchMoneyWillBeGiven = 80_000 / eventoCorrida.wonPlayers.size
                        val howMuchNightmaresWillBeGiven = if (eventoCorrida.wonPlayers.size == 1) 1 else 0

                        e.player.deposit(howMuchMoneyWillBeGiven.toDouble(),
                            TransactionContext(
                                type = TransactionType.EVENTS,
                                extra = "Corrida"
                            )
                        )

                        if (howMuchNightmaresWillBeGiven == 1)
                            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                                Cash.giveCash(e.player, howMuchNightmaresWillBeGiven.toLong(),
                                    TransactionContext(
                                        type = TransactionType.EVENTS,
                                        extra = "Corrida"
                                    )
                                )
                            }

                        e.player.fallDistance = 0.0f
                        e.player.fireTicks = 0
                        PlayerUtils.healAndFeed(e.player)

                        e.player.teleportToServerSpawnWithEffects()

                        if (howMuchNightmaresWillBeGiven == 1)
                            Bukkit.broadcastMessage("${DreamCorrida.PREFIX} §b${e.player.displayName}§a venceu a corrida em ${eventoCorrida.wonPlayers.size}º lugar! Ele ganhou §2$howMuchMoneyWillBeGiven sonecas§a e §c$howMuchNightmaresWillBeGiven pesadelo§a!")
                        else
                            Bukkit.broadcastMessage("${DreamCorrida.PREFIX} §b${e.player.displayName}§a venceu a corrida em ${eventoCorrida.wonPlayers.size}º lugar! Ele ganhou §2$howMuchMoneyWillBeGiven sonecas§a!")

                        if (eventoCorrida.wonPlayers.size == 3) { // Finalizar corrida
                            Bukkit.broadcastMessage("${DreamCorrida.PREFIX} §eEvento Corrida acabou, obrigado a todos que participaram! ^-^")

                            spawnLocation.world.players.forEach {
                                it.fallDistance = 0.0f
                                it.fireTicks = 0
                                PlayerUtils.healAndFeed(it)

                                it.teleportToServerSpawnWithEffects()
                            }

                            eventoCorrida.running = false
                            eventoCorrida.playerCheckpoints.clear()
                            eventoCorrida.lastTime = System.currentTimeMillis()
                            eventoCorrida.wonPlayers.clear()
                            eventoCorrida.damageCooldown.clear()
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
            e.player.teleportToServerSpawnWithEffects()
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
        val spawnLocation = corrida.spawn.toLocation()

        if (spawnLocation.world.name != player.world.name)
            return

        e.isCancelled = true

        if (e.cause == EntityDamageEvent.DamageCause.VOID
            || e.cause == EntityDamageEvent.DamageCause.LAVA
            || e.cause == EntityDamageEvent.DamageCause.FIRE_TICK) {

            val diff = System.currentTimeMillis() - (eventoCorrida.damageCooldown[player] ?: 0)

            if (diff >= 3_000) {
                eventoCorrida.damageCooldown[player] = System.currentTimeMillis()

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
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (!isPlayerInCorridaInteractionCheck(e.player))
            return

        // Block all item interaction in the event
        e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (!isPlayerInCorridaInteractionCheck(e.player))
            return

        // If the player is in the corrida and places a block, we will bring them back to the last checkpoint.
        //
        // We will need to do that because some players abuse lag and uses the "fake" blocks (blocks that were
        // placed on the client side that the server still needs to switch them back to air) to their advantage.
        e.isCancelled = true

        val player = e.player
        val eventoCorrida = m.eventoCorrida
        val corrida = eventoCorrida.corrida ?: return

        val currentPlayerCheckpoint = eventoCorrida.playerCheckpoints[e.player]
        val teleportTo = currentPlayerCheckpoint?.spawn?.toLocation() ?: corrida.spawn.toLocation()

        player.fallDistance = 0.0f
        player.fireTicks = 0
        PlayerUtils.healAndFeed(player)

        player.teleport(teleportTo)
        player.sendMessage("§cPara que colocar blocos na corrida? É uma corrida e não evento de construção! Mas não desista, continue correndo!")

        for (staff in Bukkit.getOnlinePlayers().filter { it.hasPermission("sparklypower.soustaff") }) {
            staff.sendMessage("§cPlayer ${player.name} tentou colocar blocos na corrida, então eu movi ele para o último checkpoint! :3")
        }
    }

    /**
     * Checks if a player is in a corrida, by checking:
     *
     * * If the event is running [EventoCorrida.running]
     * * If the [EventoCorrida.corrida] object is set
     * * If the player shares the same world as [EventoCorrida.corrida]
     *
     * @param player the player that will be checked
     * @return if the player is in the corrida
     */
    private fun isPlayerInCorrida(player: Player): Boolean {
        val eventoCorrida = m.eventoCorrida
        if (!m.eventoCorrida.running)
            return false

        val corrida = eventoCorrida.corrida ?: return false
        val spawnLocation = corrida.spawn.toLocation()

        if (spawnLocation.world.name != player.world.name)
            return false

        return true
    }

    /**
     * Checks if a player is in a corrida by using [isPlayerInCorrida], but bypasses if the player has the "dreamcorrida.bypass" permission
     *
     * @param player the player that will be checked
     * @return if the player is in the corrida
     */
    private fun isPlayerInCorridaInteractionCheck(player: Player) = !player.hasPermission("dreamcorrida.bypass") && isPlayerInCorrida(player)
}