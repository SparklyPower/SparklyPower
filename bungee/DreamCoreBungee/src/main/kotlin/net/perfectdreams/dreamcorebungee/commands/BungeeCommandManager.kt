package net.perfectdreams.dreamcorebungee.commands

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Plugin
import net.perfectdreams.commands.manager.CommandContinuationType
import net.perfectdreams.commands.manager.DispatchableCommandManager
import net.perfectdreams.dreamcorebungee.DreamCoreBungee
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent

class BungeeCommandManager(val plugin: Plugin) : DispatchableCommandManager<CommandSender, SparklyBungeeCommand, SparklyBungeeDSLCommand>() {

    init {
        commandListeners.addCommandListener { commandSender, sparklyCommand ->
            // Permissões
            if (sparklyCommand.permission != null && !commandSender.hasPermission(sparklyCommand.permission)) {
                commandSender.sendMessage(DreamCoreBungee.dreamConfig.withoutPermission.toTextComponent())
                CommandContinuationType.CANCEL
            } else {
                CommandContinuationType.CONTINUE
            }
        }
    }

    val commands = mutableListOf<SparklyBungeeCommand>()

    override fun registerCommand(command: SparklyBungeeCommand) {
        plugin.proxy.pluginManager.registerCommand(plugin, BungeeCommandWrapper(this, command))

        commands.add(command)
    }

    override fun unregisterCommand(command: SparklyBungeeCommand) {
        // plugin.proxy.pluginManager.unregisterCommand(BungeeCommandWrapper(this, command))

        commands.remove(command)
    }

    override fun getRegisteredCommands(): List<SparklyBungeeCommand> {
        return commands
    }

    override suspend fun dispatch(sender: CommandSender, command: SparklyBungeeCommand, label: String, arguments: Array<String>): Boolean {
        for (subCommand in command.subcommands) {
            if (dispatch(sender, subCommand as SparklyBungeeCommand, arguments.drop(0).firstOrNull() ?: "", arguments.drop(1).toTypedArray()))
                return true
        }

        return execute(sender, command, arguments)
    }
}