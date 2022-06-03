package net.perfectdreams.dreammochilas.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreammochilas.DreamMochilas

class GetMochilaOldDamageSystemExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(GetMochilaOldDamageSystemExecutor::class) {
        object Options : CommandOptions() {
            val damageValue = integer("damage_value")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val damageValue = args[Options.damageValue]

        val item = DreamMochilas.createMochilaOldSystem(damageValue)

        player.inventory.addItem(item)
        context.sendMessage("Prontinho patrão, usando meta value $damageValue")
    }
}