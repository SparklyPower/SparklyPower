package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.tables.ClubesHomes
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.exposedpowerutils.sql.upsert
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class DeleteHomeClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    inner class Options : CommandOptions() {
        val name = optionalGreedyString("name")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val name = args[options.name]?.substringBefore(" ") ?: "clube"

        withPlayerClube(player) { clube, selfMember ->
            if (!selfMember.permissionLevel.canExecute(ClubePermissionLevel.ADMIN)) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão para fazer isto!")
                return@withPlayerClube
            }

            onAsyncThread {
                transaction(Databases.databaseNetwork) {
                    ClubesHomes.deleteWhere { ClubesHomes.clube eq clube.id and (ClubesHomes.name eq name) }
                }
            }

            player.sendMessage("${DreamClubes.PREFIX} §aCasa do clube deletada com sucesso!")
        }
    }
}