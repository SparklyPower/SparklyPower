package net.perfectdreams.dreamxizum.commands.subcommands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.battle.BattleStage
import net.perfectdreams.dreamxizum.extensions.available
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.extensions.requireDifferentPlayers

class XizumAcceptExecutor(private val plugin: DreamXizum) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(XizumAcceptExecutor::class) {
        object Options : CommandOptions() {
            val inviter = player("player").register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val inviter = args.getAndValidate(options.inviter)
        context.requireDifferentPlayers(player, inviter)

        if (!player.available) context.fail("§cVocê não pode aceitar esse xizum.")

        inviter.battle?.let {
            if (player !in it || it.stage != BattleStage.WAITING_PLAYERS) context.fail("§cVocê não foi convidado para esse xizum ou o convite foi revogado.")
            it.addToBattle(player)
        } ?: context.fail("§c${inviter.name} não está em um xizum.")
    }
}