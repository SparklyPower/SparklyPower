package net.perfectdreams.dreamcorreios.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcorreios.DreamCorreios

class CorreiosOpenExecutor(val m: DreamCorreios) : SparklyCommandExecutor() {
    companion object CorreiosExecutor : SparklyCommandExecutorDeclaration(CorreiosOpenExecutor::class) {
        object Options : CommandOptions() {
            val page = integer("page_index")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val pageIndex = args[Options.page].toInt()

        m.launchAsyncThread {
            val caixaPostal = m.retrieveCaixaPostalOfPlayerAndHold(player)
            val inventory = m.createCaixaPostalInventoryOfPlayer(player, caixaPostal, pageIndex)

            onMainThread {
                player.openInventory(inventory)
            }
        }
    }
}