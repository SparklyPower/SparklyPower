package net.perfectdreams.dreamlagstuffrestrictor.commands

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import java.io.File

class EntitySearchDumpToFileExecutor : SparklyCommandExecutor() {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val w = player.world

        val file = File("./dump-entities-${w.name}-${System.currentTimeMillis()}.txt")
        for (entity in w.entities.sortedBy { it.type }) {
            val claim = GriefPrevention.instance.dataStore.getClaimAt(entity.location, true, null)

            file.appendText("${entity.type} - In claim? ${claim != null} - ${entity.location.x}, ${entity.location.y}, ${entity.location.z}\n")
        }
        player.sendMessage("Dumped!")
    }
}