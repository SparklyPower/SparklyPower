package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import org.jetbrains.exposed.sql.transactions.transaction

class SairClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        withPlayerClube(player) { clube, selfMember ->
            if (selfMember.permissionLevel == ClubePermissionLevel.OWNER) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não pode sair, você é o dono do clube! Para deletar o seu clube, utilize §6/clube deletar§c.")
                return@withPlayerClube
            }

            onAsyncThread {
                transaction(Databases.databaseNetwork) {
                    selfMember.delete()
                }
            }

            player.sendMessage("${DreamClubes.PREFIX} §aVocê saiu do clube... bye bye!")
            clube.sendInfoOnAsyncThread("§b${player.displayName}§7 saiu do clube... :(")
        }
    }
}