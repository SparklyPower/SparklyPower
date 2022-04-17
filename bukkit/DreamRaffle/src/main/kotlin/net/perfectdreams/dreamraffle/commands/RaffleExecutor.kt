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
import net.perfectdreams.dreamraffle.tasks.RafflesManager.currentRaffle
import net.perfectdreams.dreamraffle.tasks.RafflesManager.lastWinner
import org.bukkit.Bukkit
import kotlin.math.ceil
import kotlin.random.Random

class RaffleExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(RaffleExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        with (currentRaffle.type.colors) {
            StringBuilder("${currentRaffle.prefix} ").apply {
                lastWinner?.let {
                    val winner = Bukkit.getOfflinePlayer(it.uuid).name.toString()
                    val prize = (it.raffleTickets * it.type.currency.unitaryPrice).formatted
                    val currency = it.type.currency.displayName
                    val isGirl = MeninaAPI.isGirl(it.uuid)

                    append("${if (isGirl) "A" else "O"} vencedor${if (isGirl) "a" else ""} da última rifa foi " +
                            "${highlight(winner)}. El${if (isGirl) "a" else "e"} ganhou ${highlight("$prize $currency")}.\n⤖ ")
                }

                with (currentRaffle) {
                    with (type.currency) {
                        val prize = (tickets * unitaryPrice).formatted

                        append("O valor de cada ticket nessa rifa é de ${highlight("$unitaryPrice $displayName")}. Se quiser " +
                                    "participar, digite ${highlight("/rifa comprar <tickets>")}.\n⤖ ")

                        if (tickets == 0L) player.artigo.let {
                            append("Ninguém comprou um ticket ainda. Que tal ser $it primeir$it?")
                        } else append("A recompensa está acumulada em ${highlight("$prize $displayName")}.")

                        append(" O resultado sairá em ${highlight(remainingTime)}.")

                        with (getTickets(player)) {
                            if (this == 0L && tickets > 0) {
                                val random = Random.nextDouble(0.185, 0.425)
                                val imaginaryTickets = ceil(tickets * random / ( 1 - random )).toLong() + 1
                                val chance = imaginaryTickets / (imaginaryTickets + tickets.toDouble())

                                append("\n⤖ Sabia que se você comprasse ${highlight(imaginaryTickets.pluralize("ticket"))}, " +
                                        "você teria ${highlight(chance.percentage)} de chance de vencer a rifa?")
                            }

                            if (this > 0) append("\n⤖ Como você tem ${highlight(pluralize("ticket"))}, sua chance de " +
                                    "vencer é de ${highlight((toDouble() / tickets).percentage)}.")
                        }
                    }
                }

                player.sendMessage(toString())
            }
        }
    }
}