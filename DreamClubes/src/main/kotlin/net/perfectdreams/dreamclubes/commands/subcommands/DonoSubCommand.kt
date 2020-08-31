package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamcore.utils.Databases
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class DonoSubCommand(val m: DreamClubes) : WithClubeSubCommand {
    override fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>) {
        val playerName = args.getOrNull(0) ?: return

        val uniqueId = Bukkit.getPlayerUniqueId(playerName)

        if (uniqueId == null) {
            player.sendMessage("${DreamClubes.PREFIX} §cPlayer inexistente!")
            return
        }

        async {
            val permissionLevel = selfMember.permissionLevel

            if (!permissionLevel.canExecute(ClubePermissionLevel.OWNER)) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@async
            }

            val thePlayer = clube.retrieveMember(uniqueId)
            if (thePlayer == null) {
                player.sendMessage("${DreamClubes.PREFIX} §cPlayer não está no clube!")
                return@async
            }

            if (thePlayer.id.value == player.uniqueId) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não pode mudar as suas próprias permissões, bobinho!")
                return@async
            }

            if (thePlayer.permissionLevel == ClubePermissionLevel.OWNER) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não pode mudar as permissões do dono, bobinho!")
                return@async
            }

            transaction(Databases.databaseNetwork) {
                clube.ownerId = uniqueId
                thePlayer.permissionLevel = ClubePermissionLevel.OWNER
                selfMember.permissionLevel = ClubePermissionLevel.MEMBER
            }

            player.sendMessage("${DreamClubes.PREFIX} §aVocê transferiu o cargo de dono(a) para §d${playerName}§a!")
            clube.sendInfo("§7§b${playerName}§7 é o novo dono do ${clube.shortName}§7!")
        }
    }
}