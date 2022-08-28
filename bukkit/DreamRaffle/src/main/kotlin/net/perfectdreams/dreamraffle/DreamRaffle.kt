package net.perfectdreams.dreamraffle

import kotlinx.serialization.json.*
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamraffle.commands.DreamRaffleExecutor
import net.perfectdreams.dreamraffle.commands.RaffleExecutor
import net.perfectdreams.dreamraffle.commands.declarations.DreamRaffleCommand
import net.perfectdreams.dreamraffle.commands.declarations.RaffleCommand
import net.perfectdreams.dreamraffle.commands.subcommands.BuyRaffleExecutor
import net.perfectdreams.dreamraffle.commands.subcommands.RaffleScheduleExecutor
import net.perfectdreams.dreamraffle.commands.subcommands.RaffleStatsExecutor
import net.perfectdreams.dreamraffle.listeners.ApplyTagListener
import net.perfectdreams.dreamraffle.raffle.RaffleCurrency
import net.perfectdreams.dreamraffle.tables.Gamblers
import net.perfectdreams.dreamraffle.tasks.RafflesManager
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

class DreamRaffle : KotlinPlugin() {
    override fun softEnable() {
        super.softEnable()

        transaction(Databases.databaseNetwork) {
            SchemaUtils.createMissingTablesAndColumns(Gamblers)
        }

        registerCommands()
        RafflesManager.start(this)
        registerEvents(ApplyTagListener())
    }

    override fun softDisable() {
        super.softDisable()
        RafflesManager.save()
    }

    private fun registerCommands() {
        registerCommand(DreamRaffleCommand())
        registerCommand(RaffleCommand(this))
    }
}