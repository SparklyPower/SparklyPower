package net.perfectdreams.dreamterrainadditions.commands

import kotlinx.coroutines.sync.withLock
import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.TimeUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import org.bukkit.entity.Player

class TempTrustListExecutor(val plugin: DreamTerrainAdditions): SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        plugin.launchAsyncThread {
            val player = context.sender as? Player
                ?: context.fail("§Você precisa ser uma pessoa real para executar este comando.")
            val claim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)
                ?: return@launchAsyncThread player.sendMessage("§cVocê precisa estar em seu terreno para conferir as pessoas em que você confiou.")

            if (!(claim.ownerName == player.name || claim.allowGrantPermission(player) == null))
                return@launchAsyncThread player.sendMessage(withoutPermission)

            val claimAdditions = plugin.getOrCreateClaimAdditionsWithId(claim.id)

            claimAdditions.temporaryTrustedPlayersMutex.withLock {
                val temporarilyTrustedPlayers = claimAdditions.temporaryTrustedPlayers
                if (temporarilyTrustedPlayers.isEmpty())
                    return@launchAsyncThread player.sendMessage("§cAtualmente você não tem nenhum jogador na sua lista de jogadores temporariamente confiáveis! Que tal adicionar um? §6/trusttemp")

                player.sendMessage("§9§lLista de pessoas que temporariamente possuem permissão no terreno ${claim.id}:")
                player.sendMessage("")
                temporarilyTrustedPlayers.forEach { (uuid, timeInMillis) ->
                    // We check if the user has explicit permission because the user may have removed the permission with GriefPrevention's "/untrust" command
                    // So, if they actually DON'T have permission to build, we won't show them in the list
                    if (claim.hasExplicitPermission(uuid, ClaimPermission.Build)) {
                        val playerName = DreamUtils.retrieveUserInfo(uuid)?.username?.uppercase() ?: uuid.toString()
                        player.sendMessage(
                            "§6$playerName §f-> §7${
                                TimeUtils.convertEpochMillisToAbbreviatedTime(
                                    timeInMillis - System.currentTimeMillis()
                                )
                            }"
                        )
                    }
                }
            }
        }
    }
}