package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamclubes.utils.toAsync
import net.perfectdreams.dreamclubes.utils.toSync
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class AceitarSubCommand(val m: DreamClubes) : SubCommand {
    override fun execute(player: Player, args: Array<String>) {
        async {
            val clube = ClubeAPI.getPlayerClube(player)
            if (clube != null) {
                player.sendMessage("§cVocê já está em um clube, você não pode entrar em outro até você sair do atual!")
                return@async
            }

            toSync()
            val pendingInvite = m.pendingInvites[player.uniqueId]

            if (pendingInvite == null) {
                player.sendMessage("§cSei não, cadê o convite?")
                return@async
            }

            m.pendingInvites.remove(player.uniqueId)

            toAsync()
            val theNewClube = transaction(Databases.databaseNetwork) {
                Clube.findById(pendingInvite)
            }

            if (theNewClube != null) {
                if (theNewClube.retrieveMembers().size >= theNewClube.maxMembers) {
                    player.sendMessage("§cO clube já chegou no limite de membros!")
                    return@async
                }

                ClubeAPI.getOrCreateClubePlayerWrapper(player.uniqueId, theNewClube)
                theNewClube.sendInfo("§b${player.name}§7 entrou no clube!")
            }
        }
    }
}