package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamclubes.utils.toSync
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerTagSubCommand(val m: DreamClubes) : WithClubeSubCommand {
    override fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>) {
        async {
            if (!selfMember.canExecute(ClubePermissionLevel.ADMIN)) { // Sem permissão
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@async
            }

            val onlinePlayer = Bukkit.getPlayerExact(args[0])

            if (onlinePlayer == null) {
                // Player inexistente!
                return@async
            }

            val clubeMember = clube.retrieveMember(onlinePlayer) ?: return@async
            val newTag = args.getOrNull(1)

            transaction(Databases.databaseNetwork) {
                clubeMember.customPrefix = newTag
            }
            toSync()

            if (newTag != null)
                player.sendMessage("§aTag alterada com sucesso!")
            else
                player.sendMessage("§aTag removida com sucesso!")
        }
    }
}