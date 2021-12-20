package net.perfectdreams.dreamterrainadditions.commands

import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.TimeUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import org.bukkit.entity.Player

class TempTrustExecutor(val plugin: DreamTerrainAdditions): SparklyCommandExecutor() {
    companion object: SparklyCommandExecutorDeclaration(TempTrustExecutor::class) {
        const val MAXIMUM_TRUST_TIME_LIMIT = 15_778_800_000L // 6 months

        object Options: CommandOptions() {
            val target = word("player")
                .register()
            val time = greedyString("time")
                .register()

        }
        override val options = Options
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(context: CommandContext, args: CommandArguments) = plugin.launchAsyncThread {
        val targetName = args[options.target] ?: context.fail("§e/temptrust <player> <tempo>")
        val rawTime: String = args[options.time] ?: context.fail("§e/temptrust <player> <tempo>")
        val player = context.sender as? Player ?: context.fail("§Você precisa ser uma pessoa real para executar este comando.")

        val claim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)
            ?: return@launchAsyncThread player.sendMessage("§cVocê precisa estar em seu terreno para conceder permissão à alguém.")
        if (!(claim.ownerName == player.name || claim.allowGrantPermission(player) == null)) {
            return@launchAsyncThread player.sendMessage(withoutPermission)
        }
        if (targetName == player.name) {
            return@launchAsyncThread context.sender.sendMessage("§cVocê não pode dar um trust temporário em você mesmo! Não é assim que você irá conseguir ter auto confiança em si mesmo...")
        }
        val targetUniqueId = DreamUtils.retrieveUserUniqueId(targetName)
        val wrappedTime = TimeUtils.convertToLocalDateTimeRelativeToNow(rawTime)
        val wrappedTimeInMillis = wrappedTime.toEpochSecond() * 1000
        val differenceBetweenTargetAndCurrentTime = wrappedTimeInMillis - System.currentTimeMillis()

        if (differenceBetweenTargetAndCurrentTime <= 0 || differenceBetweenTargetAndCurrentTime > MAXIMUM_TRUST_TIME_LIMIT) {
            return@launchAsyncThread player.sendMessage("")
        }

        val claimAdditions = plugin.getOrCreateClaimAdditionsWithId(claim.id)
        claimAdditions.temporaryTrustedPlayers[targetUniqueId] = (wrappedTime.toEpochSecond() * 1000)
        claim.setPermission(targetUniqueId.toString(), ClaimPermission.Build)
        GriefPrevention.instance.dataStore.saveClaim(claim)
        return@launchAsyncThread player.sendMessage("§aVocê concedeu permissões à §f$targetName §apara editar seu terreno temporariamente.")
    }.let { Unit }
}