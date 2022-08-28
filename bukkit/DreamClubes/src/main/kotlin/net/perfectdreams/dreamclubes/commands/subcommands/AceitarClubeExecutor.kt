package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import org.jetbrains.exposed.sql.transactions.transaction

class AceitarClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        m.launchAsyncThread {
            val clube = ClubeAPI.getPlayerClube(player)

            onMainThread {
                if (clube != null) {
                    player.sendMessage("§cVocê já está em um clube, você não pode entrar em outro até você sair do atual!")
                    return@onMainThread
                }

                val pendingInvite = m.pendingInvites[player.uniqueId]

                if (pendingInvite == null) {
                    player.sendMessage("§cSei não, cadê o convite?")
                    return@onMainThread
                }

                m.pendingInvites.remove(player.uniqueId)

                onAsyncThread {
                    val theNewClube = transaction(Databases.databaseNetwork) {
                        Clube.findById(pendingInvite)
                    }

                    if (theNewClube != null) {
                        if (theNewClube.retrieveMembers().size >= theNewClube.maxMembers) {
                            player.sendMessage("§cO clube já chegou no limite de membros!")
                            return@onAsyncThread
                        }

                        ClubeAPI.getOrCreateClubePlayerWrapper(player.uniqueId, theNewClube)
                        theNewClube.sendInfoOnAsyncThread("§b${player.name}§7 entrou no clube!")
                    }
                }
            }
        }
    }
}