package net.perfectdreams.dreammochilas.commands

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreammochilas.DreamMochilas
import org.bukkit.Bukkit
import org.bukkit.block.BlockFace
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class FakeInteractAndOpenExecutor(private val m: DreamMochilas) : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
            val delay = integer("delay")
        }

        override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val delay = args[options.delay]

        player.sendMessage("Starting Fake Interact...")

        m.schedule {
            waitFor(delay * 20L)

            // open backpack
            val ev = PlayerInteractEvent(
                player,
                Action.RIGHT_CLICK_BLOCK,
                player.inventory.itemInMainHand,
                player.location.block.getRelative(BlockFace.DOWN),
                BlockFace.NORTH
            )

            Bukkit.getPluginManager().callEvent(ev)

            // trigger sell event
            val ev2 = PlayerInteractEvent(
                player,
                Action.LEFT_CLICK_BLOCK,
                player.inventory.itemInMainHand,
                player.getTargetBlock(6),
                BlockFace.NORTH
            )

            Bukkit.getPluginManager().callEvent(ev)
            Bukkit.getPluginManager().callEvent(ev2)

            player.sendMessage("Interacted? " + ev.useInteractedBlock())
            player.sendMessage("Item In Hand? " + ev.useItemInHand())

            player.sendMessage("Interacted? " + ev2.useInteractedBlock())
            player.sendMessage("Item In Hand? " + ev2.useItemInHand())
        }
    }
}