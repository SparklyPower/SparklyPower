package net.perfectdreams.dreamcash

import net.perfectdreams.dreamcash.commands.DreamCashCommand
import net.perfectdreams.dreamcash.commands.LojaCashCommand
import net.perfectdreams.dreamcash.tables.Cashes
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DreamCash : KotlinPlugin() {
    override fun softEnable() {
        super.softEnable()

        registerCommand(DreamCashCommand(this))
        registerCommand(LojaCashCommand(this))

        transaction(Databases.databaseNetwork) {
            SchemaUtils.create(Cashes)
        }
    }

    companion object {
        const val PREFIX = "§8[§c§lPesadelos§8]§e"
    }
}