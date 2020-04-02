package net.perfectdreams.dreamcorrida.events

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.PlayerUtils
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcorrida.DreamCorrida
import net.perfectdreams.dreamcorrida.utils.toChatColor
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import net.perfectdreams.dreamcore.utils.chance
import net.perfectdreams.dreamcorrida.utils.Checkpoint
import net.perfectdreams.dreamcorrida.utils.Corrida
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class EventoCorrida(val m: DreamCorrida) : ServerEvent("Corrida", "/corrida") {
    init {
        this.requiredPlayers = 30
        this.delayBetween = 3600000 // 1 hora
        this.discordAnnouncementRole = "477979984275701760"
    }

    var corrida: Corrida? = null
    var playerCheckpoints = mutableMapOf<Player, Checkpoint>()
    var wonPlayers = mutableListOf<UUID>()

    override fun preStart() {
        val canStart = m.availableCorridas.filter { it.ready }.isNotEmpty()

        if (!canStart) {
            this.lastTime = System.currentTimeMillis()
            return
        }

        running = true
        start()
    }

    override fun start() {
        val corrida = m.availableCorridas.filter { it.ready }.random()
        this.corrida = corrida

        val spawnPoint = corrida.spawn.toLocation()

        val world = spawnPoint.world

        var idx = 0
        scheduler().schedule(m) {
            while (running) {
                if (idx % 3 == 0) {
                    Bukkit.broadcastMessage("${DreamCorrida.PREFIX} Evento Corrida começou! §6/corrida")
                }

                world.players.forEach {
                    it.fallDistance = 0.0f
                    it.fireTicks = 0
                    PlayerUtils.healAndFeed(it)
                    it.activePotionEffects.filter { it.type != PotionEffectType.SPEED && it.type != PotionEffectType.JUMP } .forEach { effect ->
                        it.removePotionEffect(effect.type)
                    }

                    it.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 200, 0, false, false))
                    it.addPotionEffect(PotionEffect(PotionEffectType.JUMP, 200, 0, false, false))
                }

                waitFor(100) // 5 segundos
                idx++
            }
        }
    }
}
