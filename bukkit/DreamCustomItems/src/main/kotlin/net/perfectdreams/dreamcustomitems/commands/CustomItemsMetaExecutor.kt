package net.perfectdreams.dreamcustomitems.commands

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import org.bukkit.persistence.PersistentDataType

class CustomItemsMetaExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(CustomItemsMetaExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        player.sendMessage("§aKeys do Item:")
        val persistentDataContainer = player.inventory.itemInMainHand.itemMeta?.persistentDataContainer
        persistentDataContainer?.keys?.forEach {
            val str = buildString {
                append(it.toString())
                append(": ")
                when {
                    persistentDataContainer.has(it, PersistentDataType.STRING) -> {
                        append(persistentDataContainer.get(it, PersistentDataType.STRING))
                    }
                    persistentDataContainer.has(it, PersistentDataType.BYTE) -> {
                        append(persistentDataContainer.get(it, PersistentDataType.BYTE))
                    }
                    else -> append("§cUnknown Type")
                }
            }

            player.sendMessage(str)
        }
    }
}