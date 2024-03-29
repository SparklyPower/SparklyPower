package net.perfectdreams.dreamterrainadditions.commands

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.withLock
import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.TimeUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import org.bukkit.entity.Player

class TempTrustExecutor(val plugin: DreamTerrainAdditions): SparklyCommandExecutor() {
    companion object {
        const val MAXIMUM_TRUST_TIME_LIMIT = 15_778_800_000L // 6 months
    }

    inner class Options: CommandOptions() {
        val target = word("player")
        val time = greedyString("time")
    }

    override val options = Options()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(context: CommandContext, args: CommandArguments) {
        plugin.launchMainThread {
            val targetName = args[options.target]
            val rawTime: String = args[options.time]
            val player = context.sender as? Player ?: context.fail("§Você precisa ser uma pessoa real para executar este comando.")

            val claim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)
                ?: return@launchMainThread player.sendMessage("§cVocê precisa estar em seu terreno para conceder permissão à alguém.")

            if (!(claim.ownerName == player.name || claim.allowGrantPermission(player) == null))
                return@launchMainThread player.sendMessage(withoutPermission)

            if (targetName == player.name)
                return@launchMainThread context.sender.sendMessage("§cVocê não pode dar um trust temporário em você mesmo! Não é assim que você irá conseguir ter auto confiança em si mesmo...")

            val targetUniqueId = onAsyncThread { DreamUtils.retrieveUserUniqueId(targetName) }
            val wrappedTime = TimeUtils.convertToLocalDateTimeRelativeToNow(rawTime)
            val wrappedTimeInMillis = wrappedTime.toEpochSecond() * 1000
            val differenceBetweenTargetAndCurrentTime = wrappedTimeInMillis - System.currentTimeMillis()

            if (differenceBetweenTargetAndCurrentTime <= 0 || differenceBetweenTargetAndCurrentTime > MAXIMUM_TRUST_TIME_LIMIT)
                return@launchMainThread player.sendMessage("§cO tempo especificado é inválido. Lembre-se que o tempo não pode ser maior que 6 meses, nem estar no passado!")

            val claimAdditions = plugin.getOrCreateClaimAdditionsWithId(claim.id)

            claimAdditions.temporaryTrustedPlayersMutex.withLock {
                claimAdditions.temporaryTrustedPlayers[targetUniqueId] = (wrappedTime.toEpochSecond() * 1000)
            }

            claim.setPermission(targetUniqueId.toString(), ClaimPermission.Build)
            GriefPrevention.instance.dataStore.saveClaim(claim)

            player.sendMessage("§aVocê concedeu permissões à §f$targetName §apara editar seu terreno (${claim.id}) temporariamente.")

            plugin.saveInAsyncTask()
        }
    }
}