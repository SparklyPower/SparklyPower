package net.perfectdreams.dreamraffle

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamchat.events.ApplyPlayerTagsEvent
import net.perfectdreams.dreamchat.utils.PlayerTag
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.formatted
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamraffle.commands.RaffleExecutor
import net.perfectdreams.dreamraffle.commands.declarations.RaffleCommand
import net.perfectdreams.dreamraffle.commands.subcommands.BuyRaffleExecutor
import net.perfectdreams.dreamraffle.raffle.Raffle
import net.perfectdreams.dreamraffle.raffle.RaffleType
import net.perfectdreams.dreamraffle.raffle.RafflesManager
import net.perfectdreams.dreamraffle.raffle.Winner
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File

class DreamRaffle : KotlinPlugin(), Listener {
    private lateinit var unfinishedRaffle: File
    lateinit var currentRaffle: Raffle
    var lastWinner: Winner? = null

    override fun softEnable() {
        super.softEnable()

        dataFolder.mkdir()
        unfinishedRaffle = File(dataFolder, "unfinished_raffle.json").apply {
            if (exists()) {
                currentRaffle = Json.decodeFromString(readText())
                delete()
            } else currentRaffle = Raffle(RaffleType.NORMAL)
        }

        lastWinner = with (File(dataFolder, "last_winner.json")) {
            if (exists()) Json.decodeFromString(readText())
            else null
        }

        registerEvents(this)
        RafflesManager.start(this)
        registerCommand(RaffleCommand, RaffleExecutor(this), BuyRaffleExecutor(this))
    }

    override fun softDisable() {
        super.softDisable()
        unfinishedRaffle.writeText(Json.encodeToString(currentRaffle))
    }

    @EventHandler
    fun onApplyTag(event: ApplyPlayerTagsEvent) {
        with (event) {
            lastWinner?.let {
                val color = it.type.colors.first
                val currency = it.type.currency.displayName
                val prize = (it.type.currency.unitaryPrice * it.raffleTickets).formatted

                if (it.uuid == player.uniqueId)
                    tags.add(
                        PlayerTag(
                            "${color}§lS",
                            "${color}§lSortud${player.artigo}",
                            listOf(
                                "§r$color${player.name}§7 venceu a última rifa e",
                                "recebeu $color$prize ${currency}§7."
                            ),
                            "/rifa",
                            false
                        )
                    )
            }
        }
    }
}