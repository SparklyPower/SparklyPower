package net.perfectdreams.dreamcore.utils.commands

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommand
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import com.mojang.brigadier.tree.RootCommandNode
import net.minecraft.commands.CommandSourceStack
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import org.bukkit.command.PluginIdentifiableCommand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CommandListener(
    private val commandManager: SparklyCommandManager
) : Listener {
    @EventHandler
    fun onBrigadierCommand(e: CommandRegisteredEvent<CommandSourceStack>) {
        val command = e.command

        if (command !is PluginIdentifiableCommand)
            return
        if (command.plugin != commandManager.plugin)
            return
        if (command !is SparklyBukkitBrigadierCommandWrapper)
            return

        e.literal = command.convertDeclarationToBrigadier(command.declaration)
            .build() as LiteralCommandNode<CommandSourceStack>
    }
}