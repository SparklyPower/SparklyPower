package net.perfectdreams.dreamcasamentos

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.gson.Gson
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcasamentos.commands.MarryCommand
import net.perfectdreams.dreamcasamentos.dao.Adoption
import net.perfectdreams.dreamcasamentos.dao.Marriage
import net.perfectdreams.dreamcasamentos.dao.Request
import net.perfectdreams.dreamcasamentos.listeners.MarryListener
import net.perfectdreams.dreamcasamentos.tables.Adoptions
import net.perfectdreams.dreamcasamentos.tables.Marriages
import net.perfectdreams.dreamcasamentos.utils.MarriageParty
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class DreamCasamentos : KotlinPlugin() {
    lateinit var config: Config

    val requests = mutableListOf<Request>()

    val marriedUsers = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<Player, Optional<Player>>()
        .asMap()

    companion object {
        const val PREFIX = "§8[§d§lCasamento§8]§e"
        lateinit var INSTANCE: DreamCasamentos
    }

    override fun onEnable() {
        super.onEnable()
        MarriageParty.INSTANCE = this
        INSTANCE = this

        transaction(Databases.databaseServer) {
            SchemaUtils.createMissingTablesAndColumns(
                Marriages,
                Adoptions
            )
        }

        registerCommand(MarryCommand(this))

        registerEvents(MarryListener(this))

        val file = File(dataFolder, "config.json")

        if (!file.exists()) {
            file.createNewFile()
            file.writeText(Gson().toJson(Config()))
        }

        config = Gson().fromJson(file.readText(), Config::class.java)

        // Spawn hearts around married players
        schedule {
            while (true) {
                switchContext(SynchronizationContext.SYNC)
                val onlinePlayers = Bukkit.getOnlinePlayers().toList()
                switchContext(SynchronizationContext.ASYNC)

                val checkedPlayers = mutableListOf<Player>()

                onlinePlayers.forEach {
                    val optionalMarriedPlayer = marriedUsers.getOrPut(it) {
                        Optional.ofNullable(
                            getMarriageFor(it)?.getPartnerOf(it)?.let { it1 -> Bukkit.getPlayer(it1) }
                        )
                    }

                    switchContext(SynchronizationContext.SYNC)

                    optionalMarriedPlayer.ifPresent { marriedPlayer ->
                        if (!checkedPlayers.contains(marriedPlayer) && marriedPlayer.isOnline && !DreamVanishAPI.isVanishedOrInvisible(it) && it.canSee(marriedPlayer) && marriedPlayer.canSee(it) && marriedPlayer.world == it.world && 128 >= it.location.distanceSquared(marriedPlayer.location)) {
                            it.world.spawnParticle(Particle.HEART, it.location.clone().add(0.0, 1.0, 0.0), 3, 2.0, 2.0, 2.0)
                            marriedPlayer.world.spawnParticle(Particle.HEART, marriedPlayer.location.clone().add(0.0, 1.0, 0.0), 3, 2.0, 2.0, 2.0)

                            checkedPlayers.add(it)
                            checkedPlayers.add(marriedPlayer)
                        }
                    }
                }

                waitFor(20)
            }
        }
    }

    fun getShipName(player1: String, player2: String): String {
        return player1.substring(0..(player1.length / 2)) + player2.substring(player2.length / 2..player2.length - 1)
    }

    fun getMarriageFor(player: Player): Marriage? {
        DreamUtils.assertAsyncThread()

        val marry = transaction(Databases.databaseServer) {
            Marriage.find { (Marriages.player1 eq player.uniqueId) or (Marriages.player2 eq player.uniqueId) }.firstOrNull()
        }

        return marry
    }

    fun getMarriageRequestFor(player: Player): Request? {
        return requests.firstOrNull { it.type == Request.RequestKind.MARRIAGE && it.target == player }
    }

    fun getAdoptionRequestFor(player: Player): Request? {
        return requests.firstOrNull { it.type == Request.RequestKind.ADOPTION && it.target == player }
    }

    fun getAdoptionStatus(player: Player): Adoption? {
        DreamUtils.assertAsyncThread()

        val adoption = transaction(Databases.databaseServer) {
            Adoption.find {
                Adoptions.player eq player.uniqueId
            }.firstOrNull()
        }

        return adoption
    }

    fun getParentsOf(player: Player): Marriage? {
        DreamUtils.assertAsyncThread()

        val adoption = getAdoptionStatus(player)

        return transaction(Databases.databaseServer) {
            adoption?.adoptedBy
        }
    }
}

class Config(var loc1: LocationWrapper = LocationWrapper(Bukkit.getWorlds().first().name, 0.0, 0.0, 0.0, 0f, 0f), var loc2: LocationWrapper = LocationWrapper(Bukkit.getWorlds().first().name, 0.0, 0.0, 0.0, 0f, 0f))
class LocationWrapper(val world: String, val x: Double, val y: Double, val z: Double, val yaw: Float, val pitch: Float)