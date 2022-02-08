package net.perfectdreams.dreamcustomitems.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.inventory.ItemStack

class CustomItemsGiveExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(CustomItemsGiveExecutor::class) {
        object Options : CommandOptions() {
            val itemName = word("item_name")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val itemName = args[Options.itemName]
        val name = itemName.toUpperCase()

        val invoke = CustomItems::class.members.first { it.name == name }.call(CustomItems) as ItemStack

        player.inventory.addItem(
            invoke.clone()
        )
        player.sendMessage("§aProntinho!")
    }
}