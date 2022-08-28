package net.perfectdreams.dreamraffle.commands.subcommands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.formatted
import net.perfectdreams.dreamcore.utils.extensions.percentage
import net.perfectdreams.dreamcore.utils.extensions.pluralize
import net.perfectdreams.dreamraffle.DreamRaffle
import net.perfectdreams.dreamraffle.dao.Gambler
import net.perfectdreams.dreamraffle.raffle.RaffleCurrency
import net.perfectdreams.dreamraffle.raffle.RaffleType
import net.perfectdreams.dreamraffle.tasks.RafflesManager.currentRaffle
import org.jetbrains.exposed.sql.transactions.transaction

class BuyRaffleExecutor(private val plugin: DreamRaffle) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
            val tickets = integer("tickets")
        }

        override val options = Options()

    private val template = "§cVocê não tem %s suficientes para comprar esses tickets."
    private val unrestrictedType = RaffleType.TURBO

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val ticketsToBuy = args[options.tickets].toLong()

        if (ticketsToBuy <= 0) context.fail("§cVocê precisa comprar pelo menos um ticket, bobinh${player.artigo}.")

        with (currentRaffle) {
            val currency = type.currency
            val cost = currency.unitaryPrice * ticketsToBuy

            /**
             * Let's check if there is more than 2000 tickets in the raffle, and if there is, do not
             * allow the player to have more than 50% of all tickets to avoid a quasi-guaranteed victory
             */
            val currentPlayerTickets = getTickets(player)
            val playerTickets = currentPlayerTickets + ticketsToBuy
            val totalOfTickets = currentPlayerTickets + tickets

            if (type != unrestrictedType && totalOfTickets > 2000)
            // Now we check if the player can REALLY buy those tickets
                if (playerTickets.toDouble() / totalOfTickets > .5) {
                    /**
                     * Okay, so the player can't buy these many tickets. Let's calculate how many tickets
                     * they can actually buy.
                     */
                    val ticketsPlayerCanBuy = tickets / 2 - getTickets(player)

                    StringBuilder("§cSe você comprar ${ticketsToBuy.pluralize("ticket")}, você terá mais de 50% de chance de vencer, e isso acaba com a diversão da rifa.").apply {
                        if (ticketsPlayerCanBuy > 0)
                            append(" Se você ainda quiser comprar tickets, o máximo que você pode comprar no momento é ${ticketsPlayerCanBuy.pluralize("ticket")}.")

                        append(" A única rifa sem limite de tickets é a ${unrestrictedType.displayName}.")
                        context.fail(toString())
                    }
                }

            plugin.schedule {
                if (currency == RaffleCurrency.MONEY) {
                    if (!player.canPay(cost.toDouble())) return@schedule player.sendMessage(template.format("sonecas"))
                    player.withdraw(cost.toDouble(), TransactionContext(type = TransactionType.BETTING, extra = "em uma rifa `${type.displayName}`"))
                } else {
                    switchContext(SynchronizationContext.ASYNC)
                    val cash = Cash.getCash(player)
                    if (cash < cost) return@schedule player.sendMessage(template.format("pesadelos"))
                    Cash.takeCash(player, cost, TransactionContext(type = TransactionType.BETTING, extra = "em uma rifa `${type.displayName}`"))
                    switchContext(SynchronizationContext.SYNC)
                }

                val hadTickets = currentPlayerTickets > 0
                addTickets(player, ticketsToBuy)

                with (type.colors) {
                    val currentTickets = getTickets(player)

                    val builder = StringBuilder("$default⤖ Você comprou ${highlight(ticketsToBuy.pluralize("ticket"))}" +
                            " por ${highlight("${cost.formatted} ${currency.displayName}")}. ")

                    if (hadTickets) builder.append("Agora você tem ${highlight(currentTickets.pluralize("ticket"))} e")
                    else builder.append("Você tem")

                    builder.append(" ${highlight((currentTickets.toDouble() / tickets).percentage)} de chance de vencer a rifa.")
                    player.sendMessage(builder.toString())
                }

                this@BuyRaffleExecutor.plugin.launchAsyncThread {
                    val uuid = player.uniqueId
                    val gambler = Gambler.fetch(uuid) ?: transaction(Databases.databaseNetwork) { Gambler.new(uuid) {} }

                    if (currency == RaffleCurrency.CASH) gambler.addSpentCash(cost) else gambler.addSpentSonecas(cost)
                }
            }
        }
    }
}