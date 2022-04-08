package net.perfectdreams.dreamxizum.commands.subcommands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import net.perfectdreams.dreamxizum.battle.BattleUserStatus
import net.perfectdreams.dreamxizum.extensions.battle
import net.perfectdreams.dreamxizum.extensions.requireDifferentPlayers

class XizumRefuseExecutor(private val plugin: DreamXizum) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(XizumRefuseExecutor::class) {
        object Options : CommandOptions() {
            val inviter = player("player").register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val inviter = args.getAndValidate(options.inviter)
        context.requireDifferentPlayers(player, inviter)

        inviter.battle?.let {
            if (player !in it) context.fail("§cVocê não foi convidado para esse xizum ou o convite foi revogado.")
            it[it.indexOf(player)]?.status?.let { s -> if (s > BattleUserStatus.PENDING) context.fail("§cVocê já aceitou o convite para esse xizum.") }
            it.remove(player)
            player.sendMessage("${DreamXizum.PREFIX} Você recusou o convite de ${highlight(inviter.name)}.")
            it.broadcastMessage("${highlight(inviter.name)} recusou o convite para o xizum.")
        } ?: context.fail("§c${inviter.name} não está em um xizum.")
    }
}