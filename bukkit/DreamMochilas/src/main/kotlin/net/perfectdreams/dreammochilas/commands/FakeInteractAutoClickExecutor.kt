package net.perfectdreams.dreammochilas.commands

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreammochilas.DreamMochilas
import org.bukkit.Bukkit
import org.bukkit.block.BlockFace
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class FakeInteractAutoClickExecutor(private val m: DreamMochilas) : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(FakeInteractAutoClickExecutor::class) {
        object Options : CommandOptions() {
            val delay = integer("delay")
                .register()
            val repeat = integer("repeat")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val delay = args[Options.delay]
        val repeat = args[Options.repeat]

        player.sendMessage("Starting Fake Auto Click...")

        m.schedule {
            repeat(repeat) {
                waitFor(delay.toLong())

                // trigger sell event
                val ev = PlayerInteractEvent(
                    player,
                    Action.LEFT_CLICK_BLOCK,
                    player.inventory.itemInMainHand,
                    player.getTargetBlock(6),
                    BlockFace.NORTH
                )

                Bukkit.getPluginManager().callEvent(ev)

                player.sendMessage("Interacted? " + ev.useInteractedBlock())
                player.sendMessage("Item In Hand? " + ev.useItemInHand())
            }

            player.sendMessage("Fim!")
        }
    }
}