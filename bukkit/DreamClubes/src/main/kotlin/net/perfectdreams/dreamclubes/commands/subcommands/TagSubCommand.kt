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

class TagSubCommand(val m: DreamClubes) : WithClubeSubCommand {
    override fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>) {
        val tag = args.getOrNull(0) ?: return

        async {
            if (!selfMember.canExecute(ClubePermissionLevel.OWNER)) { // Sem permissão
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@async
            }

            val colorizedTag = tag.translateColorCodes()
            if (!ClubeAPI.checkIfClubeCanUseTagAndSendMessages(player, clube, colorizedTag))
                return@async

            transaction(Databases.databaseNetwork) {
                clube.shortName = colorizedTag
                clube.cleanShortName = colorizedTag.stripColorCode()
            }

            toSync()

            player.sendMessage("${DreamClubes.PREFIX} §aA tag do seu clube foi alterada com sucesso!")
            clube.sendInfo("§7A tag do clube foi alterada para ${colorizedTag}§7!")
        }
    }
}