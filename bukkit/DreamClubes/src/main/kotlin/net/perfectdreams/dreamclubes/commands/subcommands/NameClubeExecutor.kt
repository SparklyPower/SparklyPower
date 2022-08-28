package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.stripColorCode
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.jetbrains.exposed.sql.transactions.transaction

class NameClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    inner class Options : CommandOptions() {
        val name = greedyString("name")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val name = args[options.name]

        withPlayerClube(player) { clube, selfMember ->
            if (!selfMember.canExecute(ClubePermissionLevel.ADMIN)) { // Sem permissão
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@withPlayerClube
            }

            val colorizedName = name.translateColorCodes()

            onAsyncThread {
                transaction(Databases.databaseNetwork) {
                    clube.name = colorizedName
                    clube.cleanName = name.stripColorCode()
                }
            }

            player.sendMessage("${DreamClubes.PREFIX} §aO nome do seu clube foi alterada com sucesso!")
            clube.sendInfoOnAsyncThread("§7O nome do clube foi alterada para ${colorizedName}§7!")
        }
    }
}