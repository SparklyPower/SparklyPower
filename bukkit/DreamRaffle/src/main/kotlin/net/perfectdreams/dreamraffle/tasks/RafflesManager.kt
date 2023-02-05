package net.perfectdreams.dreamraffle.tasks

import com.okkero.skedule.schedule
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.MeninaAPI
import net.perfectdreams.dreamcore.utils.TransactionContext
import net.perfectdreams.dreamcore.utils.TransactionType
import net.perfectdreams.dreamcore.utils.deposit
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.formatted
import net.perfectdreams.dreamcore.utils.preferences.BroadcastType
import net.perfectdreams.dreamcore.utils.preferences.broadcastMessage
import net.perfectdreams.dreamraffle.DreamRaffle
import net.perfectdreams.dreamraffle.dao.Gambler
import net.perfectdreams.dreamraffle.raffle.Raffle
import net.perfectdreams.dreamraffle.raffle.RaffleCurrency
import net.perfectdreams.dreamraffle.raffle.RaffleType
import net.perfectdreams.dreamraffle.raffle.Winner
import net.perfectdreams.dreamraffle.utils.createMessage
import net.perfectdreams.dreamraffle.utils.randomRaffleType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.ArrayDeque
import java.io.File

object RafflesManager {
    private lateinit var plugin: DreamRaffle
    private lateinit var winnerFile: File
    private lateinit var raffleFile: File
    private var iterations = 0L
    private var raffles = 1L

    lateinit var currentRaffle: Raffle
    var lastWinner: Winner? = null
    val sequence = ArrayDeque<RaffleType>()

    fun start(plugin: DreamRaffle) {
        this.plugin = plugin

        with (plugin.dataFolder) {
            mkdir()

            raffleFile = File(this, "current_raffle.json").apply {
                if (exists()) {
                    currentRaffle = Json.decodeFromString(readText())
                    sequence.offer(currentRaffle.type)
                } else {
                    sequence.offer(randomRaffleType)
                    currentRaffle = Raffle(sequence.peek())
                }
            }

            repeat(4) { sequence.add(if (it == 3) RaffleType.SPECIAL else randomRaffleType) }

            winnerFile = File(this, "last_winner.json").apply {
                if (exists()) lastWinner = Json.decodeFromString(readText())
            }
        }

        plugin.schedule {
            while (true) {
                with (currentRaffle) {
                    val colors = type.colors
                    if (++iterations % 5 == 0L) plugin.launchAsyncThread { save() }

                    if (shouldNotify) {
                        hasNotified = true
                        broadcast {
                            if (tickets == 0L) "$prefix Ninguém quis apostar na rifa e ela terminará em ${colors.highlight("um minuto")}."
                            else "$prefix Corre que ainda há tempo, a rifa vai acabar em ${colors.highlight("um minuto")}. " +
                                    "Quem sabe você não sai vitorios${it.artigo}?"
                        }
                    }

                    if (shouldEnd) end()
                }

                waitFor(20L)
            }
        }
    }

    fun end() =
        with (currentRaffle) {
            val colors = type.colors

            if (tickets == 0L) {
                lastWinner = null
                broadcast { "$prefix Como só tem gente mesquinha nesse servidor, a rifa terminou sem nenhum apostador." }
            } else {
                lastWinner = winner
                val player = Bukkit.getOfflinePlayer(lastWinner!!.uuid)
                val prize = tickets * type.currency.unitaryPrice
                val isGirl = MeninaAPI.isGirl(lastWinner!!.uuid)
                val transactionMessage = "em uma rifa `${type.displayName}`"

                if (type.currency == RaffleCurrency.MONEY) player.deposit(prize.toDouble(), TransactionContext(type = TransactionType.BETTING, extra = transactionMessage))
                else plugin.launchAsyncThread { Cash.giveCash(lastWinner!!.uuid, prize, TransactionContext(type = TransactionType.BETTING, extra = transactionMessage)) }

                broadcast {
                    "$prefix ${colors.highlight(player.name.toString())} foi ${if (isGirl) "a" else "o"} vencedor${if (isGirl) "a" else ""} " +
                    "da rifa, recebendo um total de ${colors.highlight("${prize.formatted} ${type.currency.displayName}")}. " +
                    lastWinner!!.chance.createMessage(colors, MeninaAPI.getPronome(lastWinner!!.uuid))
                }
            }

            sequence.poll()
            currentRaffle = Raffle(sequence.peek())
            sequence.offer(if (++raffles %5 == 0L) RaffleType.SPECIAL else randomRaffleType)

            plugin.launchAsyncThread {
                lastWinner?.let {
                    val gambler = Gambler.fetch(it.uuid) ?: return@let
                    gambler.addVictory()

                    val currency = it.type.currency
                    val prize = it.raffleTickets * currency.unitaryPrice

                    if (currency == RaffleCurrency.CASH) gambler.addWonCash(prize)
                    else gambler.addWonSonecas(prize)
                }

                with (winnerFile) {
                    lastWinner?.let { writeText(Json.encodeToString(it)) } ?: run { if (exists()) delete() }
                }
            }
        }

    private fun broadcast(message: (Player) -> String) = Bukkit.getOnlinePlayers().forEach {
        broadcastMessage(BroadcastType.GAMBLING_MESSAGE, message)
    }

    fun save() = raffleFile.writeText(Json.encodeToString(currentRaffle))
}