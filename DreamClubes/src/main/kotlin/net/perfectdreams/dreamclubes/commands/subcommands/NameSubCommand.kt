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
import net.perfectdreams.dreamcore.utils.stripColorCode
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class NameSubCommand(val m: DreamClubes) : WithClubeSubCommand {
    override fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>) {
        val name = args.joinToString(" ")

        async {
            if (!selfMember.canExecute(ClubePermissionLevel.OWNER)) { // Sem permissão
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@async
            }

            val colorizedName = name.translateColorCodes()

            transaction(Databases.databaseNetwork) {
                clube.name = name
                clube.cleanName = name.stripColorCode()
            }

            toSync()

            player.sendMessage("${DreamClubes.PREFIX} §aO nome do seu clube foi alterada com sucesso!")
            clube.sendInfo("§7O nome do clube foi alterada para ${colorizedName}§7!")
        }
    }
}