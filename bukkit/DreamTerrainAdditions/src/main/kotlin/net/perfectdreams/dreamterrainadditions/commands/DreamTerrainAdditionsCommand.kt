package net.perfectdreams.dreamterrainadditions.commands

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import java.util.*

class DreamTerrainAdditionsCommand(val m: DreamTerrainAdditions) : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("dreamterrainadditions")) {
        permission = "dreamterrainadditions.setup"

        this.subcommand(listOf("topclaimers")) {
            executor = TopClaimersExecutor()
        }
    }

    inner class TopClaimersExecutor : SparklyCommandExecutor() {
        override fun execute(context: CommandContext, args: CommandArguments) {
            context.sendMessage("Pegando terrenos...")

            m.launchAsyncThread {
                // the ownerID can be null (I think for admin claims? GriefPrevention docs doesn't help us)
                val playerToClaimSizes = mutableMapOf<UUID?, Long>()

                val claimList = GriefPrevention.instance.dataStore.claims.toList() // We are in an async task, let's create a copy of the original list
                for (claim in claimList) {
                    playerToClaimSizes[claim.ownerID] = playerToClaimSizes.getOrDefault(claim.ownerID, 0) + claim.area
                }

                playerToClaimSizes.entries.sortedByDescending { it.value }.take(10).forEach {
                    val userId = it.key
                    val userInfo = userId?.let { DreamUtils.retrieveUserInfo(it)?.username } ?: "*Desconhecido*"
                    context.sendMessage("${userInfo}: ${it.value} blocos")
                }

                context.sendMessage("Total de players com terrenos: ${playerToClaimSizes.size}")
            }
        }
    }
}