package net.perfectdreams.dreamraffle.commands.subcommands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.canPay
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.formatted
import net.perfectdreams.dreamcore.utils.extensions.percentage
import net.perfectdreams.dreamcore.utils.extensions.pluralize
import net.perfectdreams.dreamcore.utils.withdraw
import net.perfectdreams.dreamraffle.DreamRaffle
import net.perfectdreams.dreamraffle.raffle.RaffleCurrency

class BuyRaffleExecutor(private val plugin: DreamRaffle) : SparklyCommandExecutor() {
    private val maxTickets = 200_000
    private val template = "§cVocê não tem %s suficientes para comprar esses tickets."

    companion object : SparklyCommandExecutorDeclaration(BuyRaffleExecutor::class) {
        object Options : CommandOptions() {
            val tickets = integer("tickets").register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val tickets = args[options.tickets].toLong()
        if (tickets <= 0) context.fail("§cVocê precisa comprar pelo menos um ticket.")

        with (plugin) {
            val currency = currentRaffle.type.currency
            val cost = currency.unitaryPrice * tickets

            if (currentRaffle.getTickets(player) + tickets > maxTickets) context.fail("§cSe você comprar ${tickets.pluralize("ticket" to "tickets")}, " +
                    "você ultrapassará o limite de tickets por rifa, que é de ${maxTickets.formatted}.")

            schedule {
                if (currency == RaffleCurrency.SONECAS) {
                    if (!player.canPay(cost.toDouble())) return@schedule player.sendMessage(template.format(currency.displayName))
                    player.withdraw(cost.toDouble())
                } else {
                    switchContext(SynchronizationContext.ASYNC)
                    val cash = Cash.getCash(player)
                    if (cash < cost) return@schedule player.sendMessage(template.format(currency.displayName))
                    Cash.takeCash(player, cost)
                    switchContext(SynchronizationContext.SYNC)
                }

                currentRaffle.addTickets(player, tickets)
                with (currentRaffle.type.colors) {
                    player.sendMessage("$first⤖ Você comprou $second${tickets.pluralize("ticket" to "tickets")}$first " +
                            "por $second${cost.formatted} ${currency.displayName}$first. Sua chance de vencer a rifa é de " +
                            "$second${(currentRaffle.getTickets(player).toDouble() / currentRaffle.tickets).percentage}$first.")
                }
            }
        }
    }
}