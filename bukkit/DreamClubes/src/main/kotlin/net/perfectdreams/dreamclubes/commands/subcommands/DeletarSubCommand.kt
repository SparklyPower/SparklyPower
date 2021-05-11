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

class DeletarSubCommand(val m: DreamClubes) : WithClubeSubCommand {
    override fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>) {
        async {
            if (selfMember.permissionLevel != ClubePermissionLevel.OWNER) {
                return@async
            }

            val members = clube.retrieveMembers()
            clube.sendInfo("§7Clube foi deletado... Vamos sentir saudades, bye bye... :(")

            transaction(Databases.databaseNetwork) {
                members.onEach { it.delete() }
                clube.delete()
            }
            Bukkit.broadcastMessage("${DreamClubes.PREFIX} §eClube ${clube.shortName}§e/§b${clube.name}§e de ${player.displayName}§e foi deletado... :(")
        }
    }
}