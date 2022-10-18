package net.perfectdreams.dreamlagstuffrestrictor.commands

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import org.bukkit.entity.EntityType
import java.io.File

class EntitySearchKillOutsideExecutor : SparklyCommandExecutor() {
    companion object {
        val killTypes = listOf(
            EntityType.PIG,
            EntityType.SHEEP,
            EntityType.COW,
            EntityType.MUSHROOM_COW,
            EntityType.GOAT,
            EntityType.CHICKEN,
            EntityType.FROG,
            EntityType.TURTLE,
            EntityType.TADPOLE,
            EntityType.RABBIT,

        )
    }
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val w = player.world

        w.entities.forEach {
            if (it.type in killTypes) {
                val claim = GriefPrevention.instance.dataStore.getClaimAt(it.location, true, null)

                if (claim == null) {
                    it.remove()
                }
            }
        }

        player.sendMessage("Mobs fora de terrenos foram mortos!")
    }
}