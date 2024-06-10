package net.perfectdreams.dreamcustomitems.commands

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.perfectdreams.dreamcore.utils.SparklyNamespacedKey
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.ChiseledBookshelf
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.jvm.optionals.getOrNull

class CustomItemsGiveExecutor : SparklyCommandExecutor() {
    inner class Options : CommandOptions() {
        val itemName = word("item_name")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val itemName = args[options.itemName]
        if (itemName == "blockdebug") {
            if (true) {
                println("Current thread [command place block]: ${Thread.currentThread()} - World: ${player.world}")
                println("Current tick [command place block]: ${Bukkit.getCurrentTick()} - World: ${player.world}")
                player.location.block.type = Material.CHISELED_BOOKSHELF
                val state = player.location.block.getState(false) as ChiseledBookshelf
                state.persistentDataContainer.set(SparklyNamespacedKey("custom_block"), PersistentDataType.STRING, "server_asphalt")
                state.update()
                return
            }
        }

        val name = itemName.toUpperCase()

        val invoke = CustomItems::class.members.first { it.name == name }.call(CustomItems) as ItemStack

        player.inventory.addItem(
            invoke.clone()
        )
        player.sendMessage("§aProntinho!")
    }
}