package net.perfectdreams.dreamlagstuffrestrictor.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class EntitySearchAllExecutor : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val w = player.world

        player.sendMessage(w.toString())
        player.sendMessage("Total entities in World: ${w.entityCount}")
        val chunks = w.loadedChunks
        player.sendMessage("Chunks: ${chunks.size}")

        val x = chunks.toMutableList().sortedByDescending { it.entities.size }
        player.sendMessage("Entities:")
        x.take(10).forEach {
            val worstMobTypes = it.entities.groupingBy { it.type }.eachCount()
            val worstMobType = worstMobTypes.entries.sortedByDescending { it.value }.first().key
            player.sendMessage("Chunk ${it.x}, ${it.z} (${it.x * 16}, ${it.z * 16}) (${worstMobType}) - ${it.entities.size} mobs")
        }
    }
}