package net.perfectdreams.dreamcore.utils.commands

import org.bukkit.event.Listener

class CommandListener(
    private val commandManager: SparklyCommandManager
) : Listener {
    /* @EventHandler
    fun onBrigadierCommand(e: CommandRegisteredEvent<CommandSourceStack>) {
        val command = e.command
        if (command !is PluginIdentifiableCommand)
            return
        if (command.plugin != commandManager.plugin)
            return
        if (command !is SparklyBukkitBrigadierCommandWrapper)
            return

        e.literal = command.convertRootDeclarationToBrigadier(command.declaration)
            .build() as LiteralCommandNode<CommandSourceStack>
    } */
}