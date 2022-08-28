package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamclubes.utils.toSync
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.jetbrains.exposed.sql.transactions.transaction

class CreateClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    inner class Options : CommandOptions() {
        val tagAndName = greedyString("tag_and_name")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val tagAndName = args[options.tagAndName].split(" ")
        val tag = tagAndName.getOrNull(0)
        val name = tagAndName.drop(1).joinToString(" ")

        if (tag == null || tag.isBlank()) {
            player.sendMessage("${DreamClubes.PREFIX} §cA tag do seu clube é inválida!")
            return
        }

        if (name.isBlank()) {
            player.sendMessage("${DreamClubes.PREFIX} §cO nome do seu clube é inválido!")
            return
        }

        if (150_000 > player.balance) {
            player.sendMessage("${DreamClubes.PREFIX} §cVocê precisa ter 150000 sonecas para criar um clube!")
            return
        }

        m.launchAsyncThread {
            val clube = ClubeAPI.getPlayerClube(player)

            if (clube != null) {
                onMainThread {
                    player.sendMessage("${DreamClubes.PREFIX} §cVocê já está em um clube!")
                }
                return@launchAsyncThread
            }

            val coloredTag = tag.translateColorCodes()
            val coloredName = name.translateColorCodes()
            val cleanName = name.stripColorCode()
            val cleanTag = ChatColor.stripColor(coloredTag)!!

            if (!ClubeAPI.checkIfClubeCanUseTagAndSendMessages(player, clube, coloredTag))
                return@launchAsyncThread

            transaction(Databases.databaseNetwork) {
                val theNewClube = Clube.new {
                    this.name = coloredName
                    this.cleanName = cleanName
                    this.shortName = coloredTag
                    this.cleanShortName = cleanTag
                    this.createdAt = System.currentTimeMillis()
                    this.ownerId = player.uniqueId
                }

                ClubeAPI.getOrCreateClubePlayerWrapper(player.uniqueId, theNewClube).apply {
                    this.permissionLevel = ClubePermissionLevel.OWNER
                }
            }
            // Clube criado, yay!

            onMainThread {
                player.withdraw(150_000.00, TransactionContext(extra = "criar um clube"))

                Bukkit.broadcastMessage("${DreamClubes.PREFIX} §eClube $coloredTag§e/§b$coloredName§e de ${player.displayName}§e foi criado!")
            }
        }
    }
}