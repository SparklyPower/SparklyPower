package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import org.jetbrains.exposed.sql.transactions.transaction

class KickClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    inner class Options : CommandOptions() {
        val playerName = word("player_name")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val playerName = args[options.playerName]

        withPlayerClube(player) { clube, selfMember ->
            if (!selfMember.canExecute(ClubePermissionLevel.ADMIN)) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@withPlayerClube
            }

            val uniqueId = onAsyncThread { DreamUtils.retrieveUserUniqueId(playerName) }

            val clubeMember = onAsyncThread { clube.retrieveMember(uniqueId) } ?: run {
                player.sendMessage("${DreamClubes.PREFIX} §cO player não está no seu clube!")
                return@withPlayerClube
            }

            if (clubeMember.permissionLevel == ClubePermissionLevel.ADMIN || clubeMember.permissionLevel == ClubePermissionLevel.OWNER) {
                player.sendMessage("${DreamClubes.PREFIX} §cO player tem permissão de administrador!")
                return@withPlayerClube
            }

            onAsyncThread {
                transaction(Databases.databaseNetwork) {
                    clubeMember.delete()
                }
            }

            player.sendMessage("${DreamClubes.PREFIX} §aUsuário expulso do clube!")
            clube.sendInfoOnAsyncThread("§b${playerName}§7 foi expulso do clube por §b${player.displayName}§7... :(")
        }
    }
}