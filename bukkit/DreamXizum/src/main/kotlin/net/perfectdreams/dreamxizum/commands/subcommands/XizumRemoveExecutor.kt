package net.perfectdreams.dreamxizum.commands.subcommands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamxizum.DreamXizum
import net.perfectdreams.dreamxizum.DreamXizum.Companion.highlight
import net.perfectdreams.dreamxizum.battle.BattleDeathReason
import net.perfectdreams.dreamxizum.battle.BattleUserStatus
import net.perfectdreams.dreamxizum.extensions.requireAuthoringBattle
import net.perfectdreams.dreamxizum.extensions.requireDifferentPlayers

class XizumRemoveExecutor(private val plugin: DreamXizum) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(XizumRemoveExecutor::class) {
        object Options : CommandOptions() {
            val invitee = player("player").register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val battle = context.requireAuthoringBattle(player)
        val invitee = args.getAndValidate(options.invitee)
        context.requireDifferentPlayers(player, invitee)

        if (invitee !in battle || battle[battle.indexOf(invitee)]?.status == BattleUserStatus.DECEASED) context.fail("§c${invitee.name} não está nesse xizum.")
        battle.removeFromBattle(invitee, BattleDeathReason.TELEPORTED)
        invitee.sendMessage("§cVocê foi expulso do xizum.")
        battle.broadcastMessage("${highlight(invitee.name)} foi expulso do xizum.")
    }
}