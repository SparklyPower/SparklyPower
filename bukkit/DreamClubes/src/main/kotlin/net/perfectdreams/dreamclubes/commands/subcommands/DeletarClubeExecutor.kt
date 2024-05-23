package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.tables.ClubeHomeUpgrades
import net.perfectdreams.dreamclubes.tables.ClubesHomes
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class DeletarClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        withPlayerClube(player) { clube, selfMember ->
            if (selfMember.permissionLevel != ClubePermissionLevel.OWNER) {
                return@withPlayerClube
            }

            onAsyncThread {
                val members = clube.retrieveMembers()
                clube.sendInfoOnAsyncThread("§7Clube foi deletado... Vamos sentir saudades, bye bye... :(")

                transaction(Databases.databaseNetwork) {
                    members.onEach { it.delete() }
                    ClubesHomes.deleteWhere { ClubesHomes.clube eq clube.id }
                    ClubeHomeUpgrades.deleteWhere { ClubeHomeUpgrades.clube eq clube.id }
                    clube.delete()
                }

                Bukkit.broadcastMessage("${DreamClubes.PREFIX} §eClube ${clube.shortName}§e/§b${clube.name}§e de ${player.displayName}§e foi deletado... :(")
            }
        }
    }
}