package net.perfectdreams.dreamclubes.listeners

import kotlinx.coroutines.Dispatchers
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.tables.PlayerDeaths
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.exposedpowerutils.sql.transaction
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.jetbrains.exposed.sql.insert
import java.time.Instant

class DeathListener(val m: DreamClubes) : Listener {
    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val killed = e.player
        if (killed.world.name != "RealArenasPvP")
            return

        val killer = e.player.killer

        m.launchAsyncThread {
            transaction(Dispatchers.IO, Databases.databaseNetwork) {
                PlayerDeaths.insert {
                    it[PlayerDeaths.killed] = killed.uniqueId
                    it[PlayerDeaths.killer] = killer?.uniqueId
                    it[PlayerDeaths.time] = Instant.now()
                }
            }
        }
    }
}