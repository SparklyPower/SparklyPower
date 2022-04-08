package net.perfectdreams.dreamraffle.commands

import net.perfectdreams.dreamcore.utils.MeninaAPI
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.formatted
import net.perfectdreams.dreamcore.utils.extensions.percentage
import net.perfectdreams.dreamcore.utils.extensions.pluralize
import net.perfectdreams.dreamraffle.DreamRaffle
import net.perfectdreams.dreamraffle.raffle.RafflesManager
import org.bukkit.Bukkit
import kotlin.math.ceil
import kotlin.random.Random

class RaffleExecutor(private val plugin: DreamRaffle) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(RaffleExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        with (plugin) {
            if (currentRaffle.shouldEnd) RafflesManager.end(currentRaffle)

            with (currentRaffle.type.colors) {
                StringBuilder("$first⤖ ").apply {
                    lastWinner?.let {
                        val winner = Bukkit.getOfflinePlayer(it.uuid).name
                        val prize = (it.raffleTickets * it.type.currency.unitaryPrice).formatted
                        val currency = it.type.currency.displayName
                        val isGirl = MeninaAPI.isGirl(it.uuid)

                        append("${if (isGirl) "A" else "O"} vencedor${if (isGirl) "a" else ""} da última rifa foi " +
                                "$second$winner$first. El${if (isGirl) "a" else "e"} ganhou $second$prize $currency$first.\n⤖ ")
                    }

                    with (currentRaffle) {
                        with (type.currency) {
                            val prize = (tickets * unitaryPrice).formatted

                            append("O valor de cada ticket nessa rifa é de $second$unitaryPrice $displayName$first. Se quiser " +
                                        "participar, digite $second/rifa comprar <tickets>$first.\n⤖ ")

                            if (tickets == 0L) player.artigo.let {
                                append("Ninguém comprou um ticket ainda. Que tal ser $it primeir$it?")
                            } else append("A recompensa está acumulada em $second$prize $displayName$first. O resultado sairá " +
                                    "em $second${remainingTime}$first.")

                            with (getTickets(player)) {
                                if (this == 0L && tickets > 0) {
                                    val random = Random.nextDouble(0.125, 0.35)
                                    val imaginaryTickets = ceil(tickets * random / ( 1 - random )).toLong() + 1
                                    val chance = imaginaryTickets / (imaginaryTickets + tickets.toDouble())

                                    append("\n⤖ Sabia que se você comprasse $second${imaginaryTickets.pluralize("ticket" to "tickets")}$first, " +
                                            "você teria $second${chance.percentage}$first de chance de vencer a rifa?")
                                }

                                if (this > 0) append("\n⤖ Como você tem $second${pluralize("ticket" to "tickets")}$first, sua chance de " +
                                        "vencer é de $second${(toDouble() / tickets).percentage}$first.")
                            }
                        }
                    }

                    player.sendMessage(toString())
                }
            }
        }
    }
}