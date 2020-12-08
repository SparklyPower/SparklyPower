package net.perfectdreams.dreamcustomitems.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcustomitems.DreamCustomItems
import net.perfectdreams.dreamcustomitems.utils.CustomItems
import org.bukkit.inventory.ItemStack

object CustomItemsCommand : DSLCommandBase<DreamCustomItems> {
    override fun command(plugin: DreamCustomItems) = create(
        listOf("customitems")
    ) {
        permission = "dreamcustomitems.setup"

        executes {
            val name = args[0].toUpperCase()

            val invoke = CustomItems::class.members.first { it.name == name }.call(CustomItems) as ItemStack

            player.inventory.addItem(
                invoke.clone()
            )
            player.sendMessage("§aProntinho!")
        }
    }
}