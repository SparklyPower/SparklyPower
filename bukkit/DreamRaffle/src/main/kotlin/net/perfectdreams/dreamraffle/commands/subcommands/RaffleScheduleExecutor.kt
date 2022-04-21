package net.perfectdreams.dreamraffle.commands.subcommands

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.toBaseComponent
import net.perfectdreams.dreamraffle.tasks.RafflesManager.sequence
import net.perfectdreams.dreamraffle.tasks.RafflesManager.currentRaffle
import net.perfectdreams.dreamraffle.utils.remainingTime
import java.awt.Color
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.TimeZone

class RaffleScheduleExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(RaffleScheduleExecutor::class)

    private val dateFormat = SimpleDateFormat("HH:mm").apply {
        timeZone = TimeZone.getTimeZone(ZoneId.of("America/Sao_Paulo"))
    }

    private val firstLine = ComponentBuilder("➠ ").color(ChatColor.of(Color(0xE883FC)))
        .append("Cronograma das rifas").underlined(true).append(":\n").underlined(false)

    private val messages = listOf<Message>(
        { "A rifa atual termina em ${it.time}" },
        { "Em seguida, terá uma rifa ${it.type}" },
        { "Dentro de ${it.time}, ocorrerá uma rifa ${it.type}" },
        { "Daqui ${it.time}, haverá uma rifa ${it.type}" },
        { "Finalmente, em ${it.time}, uma rifa ${it.type}" }
    )

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val message = ComponentBuilder(firstLine)
        var time = currentRaffle.end - System.currentTimeMillis()

        sequence.forEachIndexed { index, it ->
            val duration = it.duration.toMillis()
            if (index > 0) time += duration

            val displayTime = time - if (index > 0) duration else 0

            val color = it.colors.default
            val context = RaffleContext(it.displayName, displayTime.remainingTime)
            message.append("\n➵ ").reset().color(it.colors.default)
                .append(messages[index].invoke(context) + ".")
                .event(HoverEvent(SHOW_TEXT, (
                        "§7Nessa rifa, aposte com $color${it.currency.displayName}§7.\n" +
                        "§7Ela tem uma duração de $color${it.duration.toMinutes()} minutos" +
                        if (index > 0) "\n§7e terá início às $color${dateFormat.format(System.currentTimeMillis() + displayTime)}§7."
                        else "§7."
                    ).toBaseComponent()
                ))
        }

        player.sendMessage(*message.create())
    }
}

private data class RaffleContext(val type: String, val time: String)
private typealias Message = (RaffleContext) -> String