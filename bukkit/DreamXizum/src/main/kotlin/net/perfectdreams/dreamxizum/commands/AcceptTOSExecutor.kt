package net.perfectdreams.dreamxizum.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.extensions.playSoundAndSendMessage
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.battle.Matchmaker
import net.perfectdreams.dreamxizum.dao.Combatant
import org.bukkit.Sound

class AcceptTOSExecutor(private val plugin: DreamXizum) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(AcceptTOSExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        plugin.schedule(SynchronizationContext.ASYNC) {
            if (!Matchmaker.hasSeasonStarted) return@schedule player.sendMessage("§cA temporada não se iniciou ainda.")

            val combatant = Combatant.fetch(player.uniqueId)
            if (combatant.tos) return@schedule player.sendMessage("§cVocê já aceitou os termos para jogar no xizum competitivo.")
            combatant.acceptTOS()
            switchContext(SynchronizationContext.SYNC)
            player.playSoundAndSendMessage(Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                "${DreamXizum.PREFIX} Agora você pode jogar no modo competitivo.")
        }
    }
}