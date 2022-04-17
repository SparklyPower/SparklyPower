package net.perfectdreams.dreamraffle

import kotlinx.serialization.json.*
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.deposit
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamraffle.commands.DreamRaffleExecutor
import net.perfectdreams.dreamraffle.commands.RaffleExecutor
import net.perfectdreams.dreamraffle.commands.declarations.DreamRaffleCommand
import net.perfectdreams.dreamraffle.commands.declarations.RaffleCommand
import net.perfectdreams.dreamraffle.commands.subcommands.BuyRaffleExecutor
import net.perfectdreams.dreamraffle.commands.subcommands.RaffleScheduleExecutor
import net.perfectdreams.dreamraffle.listeners.ApplyTagListener
import net.perfectdreams.dreamraffle.raffle.RaffleCurrency
import net.perfectdreams.dreamraffle.tasks.RafflesManager
import java.io.File
import java.util.*

class DreamRaffle : KotlinPlugin() {
    override fun softEnable() {
        super.softEnable()

        // Temporary measure to finish legacy raffle
        with (File(dataFolder, "unfinished_raffle.json")) {
            if (exists()) {
                val element = Json.parseToJsonElement(readText())
                with(element.jsonObject) {
                    val type = get("type")!!.jsonPrimitive.content
                    val currency = if (type == "CASH") RaffleCurrency.CASH else RaffleCurrency.MONEY

                    get("participants")?.jsonArray?.forEach {
                        val gambler = it.jsonObject
                        val serializedUUID = gambler["uuid"]!!.jsonPrimitive.content
                        val tickets = gambler["tickets"]!!.jsonPrimitive.long

                        val uuid = UUID.fromString(serializedUUID)

                        if (currency == RaffleCurrency.CASH) {
                            val cash = tickets * 25
                            launchAsyncThread { Cash.giveCash(uuid, cash) }
                        } else {
                            val money = tickets * 250
                            server.getOfflinePlayer(uuid).deposit(money.toDouble())
                        }
                    }
                }
                delete()
            }
        }

        with (File(dataFolder, "last_winner.json")) { if (exists()) delete() }

        RafflesManager.start(this)

        registerEvents(ApplyTagListener())
        registerCommand(DreamRaffleCommand, DreamRaffleExecutor())
        registerCommand(RaffleCommand, RaffleExecutor(), BuyRaffleExecutor(this), RaffleScheduleExecutor())
    }

    override fun softDisable() {
        super.softDisable()
        RafflesManager.save()
    }
}