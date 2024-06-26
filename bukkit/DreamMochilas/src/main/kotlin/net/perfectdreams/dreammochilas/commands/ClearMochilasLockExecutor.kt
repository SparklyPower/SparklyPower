package net.perfectdreams.dreammochilas.commands

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.utils.MochilaUtils

class ClearMochilasLockExecutor(private val m: DreamMochilas) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val mochilaId = integer("mochila_id")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val mochilaId = args[options.mochilaId].toLong()
        val mochilaData = MochilaUtils.loadedMochilas.remove(mochilaId)
        if (mochilaData == null) {
            context.sendMessage("A mochila $mochilaId não possui locks!")
        } else {
            context.sendMessage("Locks da mochila $mochilaId foram removidos! - Locks que a mochila tinha: ${mochilaData.holds}")
            context.sendMessage("§cCUIDADO QUE ISSO PODE CAUSAR PROBLEMAS DE DUPE CASO A MOCHILA AINDA ESTIVESSE ABERTA")
        }
    }
}