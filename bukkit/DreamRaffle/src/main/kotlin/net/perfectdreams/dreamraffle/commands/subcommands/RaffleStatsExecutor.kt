package net.perfectdreams.dreamraffle.commands.subcommands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.MeninaAPI
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.formatted
import net.perfectdreams.dreamraffle.DreamRaffle
import net.perfectdreams.dreamraffle.dao.Gambler
import net.perfectdreams.dreamraffle.tasks.RafflesManager
import org.bukkit.Bukkit

class RaffleStatsExecutor(private val plugin: DreamRaffle) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
            val target = optionalWord("jogador")
        }

        override val options = Options()

    private val stats = listOf("Vitórias", "Sonecas apostadas", "Sonecas recebidas", "Pesadelos apostados", "Pesadelos recebidos")

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val target = args[options.target]?.let { name ->
            Bukkit.getOfflinePlayer(name).also {
                if (!it.hasPlayedBefore()) context.fail("§c$name nunca jogou no SparklyPower.")
            }
        } ?: player

        val checkingSelf = target == player
        val name = if (checkingSelf) "Você" else target.name ?: "???"
        val pronoun = if (checkingSelf) "você" else MeninaAPI.getPronome(target.uniqueId)

        plugin.schedule(SynchronizationContext.ASYNC) {
            val gambler = Gambler.fetch(target.uniqueId) ?: return@schedule player.sendMessage("§c$name nunca apostou na rifa.")
            val gamblerStats = with (gambler) { listOf(victories, spentSonecas, wonSonecas, spentCash, wonCash) }
            val sonecasProfit = with (gambler) { wonSonecas - spentSonecas }
            val cashProfit = with (gambler) { wonCash - spentCash }

            switchContext(SynchronizationContext.SYNC)

            val colors = RafflesManager.currentRaffle.type.colors

            StringBuilder("${colors.default}➠ §n").apply {
                if (checkingSelf) append("Suas estatísicas") else append("Estatísicas de ${colors.highlight(name)}")
                append("${colors.default}:\n")

                repeat(5) { append("${colors.default}\n➵ ${stats[it]}: ${colors.highlight(gamblerStats[it].toLong().formatted)}") }

                if (sonecasProfit == 0L && cashProfit == 0L) return@schedule player.sendMessage(toString())
                append("\n\n${colors.default}• No geral, $pronoun ")

                val sonecas = sonecasProfit.formatted
                val cash = cashProfit.formatted

                when {
                    sonecasProfit < 0 && cashProfit < 0 -> append(
                        "só teve prejuízo, perdendo ${colors.highlight("$sonecas sonecas")} " +
                                "e ${colors.highlight("$cash pesadelos")}"
                    )

                    sonecasProfit > 0 && cashProfit > 0 -> append(
                        "só teve lucros, ganhando ${colors.highlight("$sonecas sonecas")} " +
                                "e ${colors.highlight("$cash pesadelos")}"
                    )

                    else -> {
                        if (sonecasProfit > 0) append("lucrou ${colors.highlight("$sonecas sonecas")}")
                        else if (sonecasProfit < 0) append("perdeu ${colors.highlight("$sonecas sonecas")}")

                        if (sonecasProfit != 0L && cashProfit != 0L) append(" e ")

                        if (cashProfit > 0) append("lucrou ${colors.highlight("$cash pesadelos")}")
                        else if (cashProfit < 0) append("perdeu ${colors.highlight("$cash pesadelos")}")
                    }
                }

                player.sendMessage("${toString()}.")
            }
        }
    }
}