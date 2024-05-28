package net.perfectdreams.dreamsonecas

import net.milkbowl.vault.economy.Economy
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.VaultUtils
import net.perfectdreams.dreamsonecas.commands.SonecasCommand
import net.perfectdreams.dreamsonecas.commands.SonecasAdminCommand
import net.perfectdreams.dreamsonecas.tables.PlayerSonecas
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DreamSonecas : KotlinPlugin() {
    private val economy = SonecasEconomy(this)

    override fun softEnable() {
        super.softEnable()

        Bukkit.getServicesManager().register(Economy::class.java, economy, this, ServicePriority.Normal)
        // TODO: This is a hack because DreamCore setups the economy before DreamSonecas is loaded
        VaultUtils.setupEconomy()
        registerCommand(SonecasCommand(this))
        registerCommand(SonecasAdminCommand(this))

        transaction(Databases.databaseNetwork) {
            SchemaUtils.createMissingTablesAndColumns(
                PlayerSonecas
            )
        }
    }
}