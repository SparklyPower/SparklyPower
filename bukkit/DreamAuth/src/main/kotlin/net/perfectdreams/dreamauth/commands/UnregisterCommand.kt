package net.perfectdreams.dreamauth.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamauth.dao.AuthInfo
import net.perfectdreams.dreamauth.tables.AuthStorage
import net.perfectdreams.dreamcore.utils.Databases
import org.bukkit.command.CommandSender
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalStateException
import java.util.*

class UnregisterCommand : SparklyCommand(arrayOf("unregister"), permission = "dreamauth.unregister") {

    @Subcommand
    fun root(sender: CommandSender) {
        sender.sendMessage("§c/unregister <player>")
    }

    @Subcommand
    fun unregister(sender: CommandSender, playerName: String) {
        val id = try {
            UUID.fromString(playerName)
        } catch (e: IllegalStateException) {
            UUID.nameUUIDFromBytes("OfflinePlayer:$playerName".toByteArray())
        }

        transaction(Databases.databaseNetwork) {
            AuthStorage.deleteWhere { AuthStorage.uniqueId eq id }
        }

        sender.sendMessage("§cConta de §e$id§c resetada com sucesso!")
    }
}