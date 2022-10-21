package net.perfectdreams.dreamquickharvest.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamquickharvest.DreamQuickHarvest
import org.bukkit.Material

class FixStemExecutor(val m: DreamQuickHarvest) : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val chunk = player.chunk
        for (x in 0 until 16) {
            for (z in 0 until 16) {
                for (y in chunk.world.minHeight until chunk.world.maxHeight) {
                    val block = chunk.getBlock(x, y, z)
                    if (block.type == Material.ATTACHED_MELON_STEM) {
                        block.type = Material.MELON_STEM
                    }
                    if (block.type == Material.ATTACHED_PUMPKIN_STEM) {
                        block.type = Material.PUMPKIN_STEM
                    }
                }
            }
        }

        player.sendMessage("§aOs caules foram arrumados! (Eu acho)")
    }
}