package net.perfectdreams.dreammochilas.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreammochilas.DreamMochilas

class GetMochilaOldDamageSystemExecutor : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
            val damageValue = integer("damage_value")
        }

        override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val damageValue = args[options.damageValue]

        val item = DreamMochilas.createMochilaOldSystem(damageValue)

        player.inventory.addItem(item)
        context.sendMessage("Prontinho patrão, usando meta value $damageValue")
    }
}