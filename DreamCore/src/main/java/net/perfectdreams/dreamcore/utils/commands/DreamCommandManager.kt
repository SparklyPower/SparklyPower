package net.perfectdreams.dreamcore.utils.commands

import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.plugin.Plugin

class DreamCommandManager(val plugin: Plugin) {
    val commands = mutableListOf<BukkitCommandWrapper>()

    private fun getCommandMap(): CommandMap {
        return Bukkit.getCommandMap()
    }

    fun registerCommand(command: Command<CommandContext>): BukkitCommandWrapper {
        plugin.logger.info { "Registering ${command.labels.first()}..." }

        val firstLabel = command.labels.first().split(" ").first()
        val matchingCommands = commands.filter { it.command.labels.any { it.split(" ").first() == firstLabel } }

        if (matchingCommands.isNotEmpty()) {
            val matchingCommand = matchingCommands.first()

            plugin.logger.info { "There are $matchingCommand that matches ${command.labels.first()}, reusing $matchingCommand..." }
            matchingCommand.commands.add(command)
            return matchingCommand
        }

        val cmd = BukkitCommandWrapper(plugin, command)
        commands.add(cmd)
        this.getCommandMap().register(cmd.label, cmd)
        return cmd
    }

    fun unregisterCommand(command: BukkitCommandWrapper) {
        try {
            val knownCommands = this.getCommandMap().knownCommands
            knownCommands.filter { it.value in commands }.forEach {
                plugin.logger.info { "Unregistering ${it.key}..." }
                knownCommands.remove(it.key)
            }
            commands.remove(command)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregisterAll() {
        commands.toList().forEach { unregisterCommand(it) }
    }
}