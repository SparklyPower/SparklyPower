package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamclubes.utils.toSync
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.stripColorCode
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerTagClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    inner class Options : CommandOptions() {
        val playerName = player("player")
        val tag = optionalGreedyString("tag")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        withPlayerClube(player) { clube, selfMember ->
            if (!selfMember.canExecute(ClubePermissionLevel.ADMIN)) { // Sem permissão
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão!")
                return@withPlayerClube
            }

            val onlinePlayer = args[options.playerName]
            val newTag = args[options.tag]

            val clubeMember = onAsyncThread { clube.retrieveMember(onlinePlayer) } ?: return@withPlayerClube
            onAsyncThread {
                transaction(Databases.databaseNetwork) {
                    clubeMember.customPrefix = newTag
                }
            }

            if (newTag != null)
                player.sendMessage("§aTag alterada com sucesso!")
            else
                player.sendMessage("§aTag removida com sucesso!")
        }
    }
}