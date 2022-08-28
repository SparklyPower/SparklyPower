package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.stripColorCode
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

class TagClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    inner class Options : CommandOptions() {
        val tag = greedyString("tag")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val tag = args[options.tag].substringBefore(" ")

        withPlayerClube(player) { clube, selfMember ->
            if (!selfMember.canExecute(ClubePermissionLevel.ADMIN)) { // Sem permissão
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@withPlayerClube
            }

            val colorizedTag = tag.translateColorCodes()
            val success = onAsyncThread {
                changeTag(player, clube, colorizedTag)
            }
            if (!success)
                return@withPlayerClube

            player.sendMessage("${DreamClubes.PREFIX} §aO nome do seu clube foi alterada com sucesso!")
            clube.sendInfoOnAsyncThread("§7O nome do clube foi alterada para ${colorizedTag}§7!")
        }
    }

    private fun changeTag(player: Player, clube: Clube, colorizedTag: String): Boolean {
        if (!ClubeAPI.checkIfClubeCanUseTagAndSendMessages(player, clube, colorizedTag))
            return false

        transaction(Databases.databaseNetwork) {
            clube.shortName = colorizedTag
            clube.cleanShortName = colorizedTag.stripColorCode()
        }
        return true
    }
}