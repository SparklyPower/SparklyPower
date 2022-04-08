package net.perfectdreams.dreamraffle.raffle

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.deposit
import net.perfectdreams.dreamcore.utils.extensions.formatted
import net.perfectdreams.dreamraffle.DreamRaffle
import java.io.File

object RafflesManager {
    private lateinit var plugin: DreamRaffle
    private val sequence = listOf(RaffleType.NORMAL, RaffleType.NORMAL, RaffleType.CASH, RaffleType.TURBO,
        RaffleType.NORMAL, RaffleType.CASH, RaffleType.NORMAL, RaffleType.NORMAL, RaffleType.TURBO)
    private var totalOfRaffles = 0

    fun start(plugin: DreamRaffle) {
        this.plugin = plugin
        plugin.schedule {
            while (true) {
                with (plugin.currentRaffle) { if (shouldEnd) end(this) }
                waitFor(20L * 60)
            }
        }
    }

    fun end(raffle: Raffle) =
        with (raffle) {
            plugin.schedule {
                if (shouldEnd) {
                    lateinit var message: String
                    lateinit var luckyOne: Winner

                    if (tickets == 0L) {
                        this@RafflesManager.plugin.lastWinner = null
                        message = "${type.colors.first}⤖ A rifa terminou com 0 apostadores :("
                    } else {
                        luckyOne = winner
                        val player = plugin.server.getOfflinePlayer(luckyOne.uuid)
                        val prize = tickets * type.currency.unitaryPrice

                        if (type.currency == RaffleCurrency.SONECAS)
                            player.deposit(prize.toDouble())
                        else {
                            switchContext(SynchronizationContext.ASYNC)
                            Cash.giveCash(luckyOne.uuid, prize)
                        }

                        with(type.colors) {
                            message = "$first⤖ $second${player.name} ${first}venceu a rifa e recebeu $second" +
                                    "${prize.formatted} ${type.currency.displayName}$first."
                        }

                        this@RafflesManager.plugin.lastWinner = luckyOne
                    }

                    totalOfRaffles++
                    this@RafflesManager.plugin.currentRaffle = with (sequence) { Raffle(get(totalOfRaffles % size)) }
                    plugin.server.onlinePlayers.forEach { it.sendMessage(message) }

                    switchContext(SynchronizationContext.ASYNC)
                    with (File(plugin.dataFolder, "last_winner.json")) {
                        if (tickets == 0L && exists()) delete()
                        if (tickets > 0) writeText(Json.encodeToString(luckyOne))
                    }
                }
            }
        }
}