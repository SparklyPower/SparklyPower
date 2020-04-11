package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class KickSubCommand(val m: DreamClubes) : WithClubeSubCommand {
    override fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>) {
        val playerName = args.getOrNull(0)

        async {
            if (!selfMember.canExecute(ClubePermissionLevel.ADMIN)) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@async
            }

            val uniqueId = Bukkit.getPlayerUniqueId(playerName) ?: run {
                player.sendMessage("${DreamClubes.PREFIX} §cO player não existe! Você tem certeza que colocou o nome certo?")
                return@async
            }

            val clubeMember = clube.retrieveMember(uniqueId) ?: run {
                player.sendMessage("${DreamClubes.PREFIX} §cO player não está no seu clube!")
                return@async
            }

            if (clubeMember.permissionLevel == ClubePermissionLevel.ADMIN || clubeMember.permissionLevel == ClubePermissionLevel.OWNER) {
                player.sendMessage("${DreamClubes.PREFIX} §cO player tem permissão de administrador!")
                return@async
            }

            transaction(Databases.databaseNetwork) {
                clubeMember.delete()
            }

            player.sendMessage("${DreamClubes.PREFIX} §aUsuário expulso do clube!")
            clube.sendInfo("§b${playerName}§7 foi expulso do clube por §b${player.displayName}§7... :(")
        }
    }
}