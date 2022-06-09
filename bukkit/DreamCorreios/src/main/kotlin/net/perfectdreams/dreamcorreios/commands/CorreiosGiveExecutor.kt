package net.perfectdreams.dreamcorreios.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcorreios.DreamCorreios
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class CorreiosGiveExecutor(val m: DreamCorreios) : SparklyCommandExecutor() {
    companion object CorreiosExecutor : SparklyCommandExecutorDeclaration(CorreiosGiveExecutor::class) {
        object Options : CommandOptions() {
            val player = player("player")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = args[Options.player]

        m.addItem(player, ItemStack(Material.DIAMOND, 64), ItemStack(Material.DIAMOND_BLOCK, 64))

        player.sendMessage("Itens dados!")
    }
}