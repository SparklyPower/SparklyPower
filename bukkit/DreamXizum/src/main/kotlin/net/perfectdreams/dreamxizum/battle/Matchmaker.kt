package net.perfectdreams.dreamxizum.battle

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.deposit
import net.perfectdreams.dreamcore.utils.extensions.allowAllCommands
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.extensions.freeFromBattle
import net.perfectdreams.dreamxizum.tasks.RankedQueueUser
import org.bukkit.entity.Player
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

object Matchmaker {
    val battles = mutableSetOf<Battle>()
    val rankedQueue = mutableListOf<RankedQueueUser>()

    val zone: ZoneId = ZoneId.of("America/Sao_Paulo")
    val firstSeasonStart = Date(1649620800000)
    val firstSeasonEnd = Date(1650916800000)

    val zonedSeasonEnd: ZonedDateTime = firstSeasonEnd.toInstant().atZone(zone)

    val hasSeasonStarted get() = Date().after(firstSeasonStart)

    val currentSeason get() = (if (Date().before(firstSeasonEnd)) 1 else
        ChronoUnit.WEEKS.between(zonedSeasonEnd, ZonedDateTime.now()) / 4 + 2).toInt()

    fun fetchBattles(player: Player) = battles.filter { player in it }
    fun createBattle(type: BattleType, limit: Int, author: Player? = null) = Battle(type, limit, author).apply {
        author?.battle = this
        battles.add(this)
    }

    fun cancelBattle(battle: Battle, sendAuthorToSpawn: Boolean = true) {
        battle.author?.battle = null
        battles.remove(battle)
        BattleHolograms.deleteHolograms(battle)
        battle.broadcastMessage("A partida em que você estava foi cancelada.")
        battle.players.filterNotNull().forEach {
            with (it.player) {
                if (it.status == BattleUserStatus.ALIVE) this.battle = null
                if (battle.stage > BattleStage.CREATING_BATTLE && it.status == BattleUserStatus.ALIVE) {
                    freeFromBattle()
                    if (equals(battle.author) && sendAuthorToSpawn) teleportToServerSpawn()
                    else if (!equals(battle.author)) teleportToServerSpawn()
                    if (battle.options.sonecas > 0) it.player.deposit(battle.options.sonecas)
                    if (battle.options.cash > 0) DreamXizum.INSTANCE.schedule(SynchronizationContext.ASYNC) { Cash.giveCash(it.player, battle.options.cash) }
                }
            }
        }
    }

    fun removeFromQueue(player: Player) =
        with (rankedQueue) {
            firstOrNull { it.duelist.uuid == player.uniqueId }?.let {
                remove(it)
                player.allowAllCommands()
                player.sendMessage("${DreamXizum.PREFIX} Você saiu da fila.")
            }
        }
}