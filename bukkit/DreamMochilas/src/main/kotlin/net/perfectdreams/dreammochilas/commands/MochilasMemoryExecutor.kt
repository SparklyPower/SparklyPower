package net.perfectdreams.dreammochilas.commands

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.utils.MochilaUtils

class MochilasMemoryExecutor(private val m: DreamMochilas) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(MochilasMemoryExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("Mochilas in memory (cached):")

        m.launchMainThread {
            MochilaUtils.loadedMochilas.toList().sortedBy { it.first }.forEach {
                context.sendMessage("${it.first} - locks: ${it.second.heldLocksCount()}")
            }
        }
    }
}