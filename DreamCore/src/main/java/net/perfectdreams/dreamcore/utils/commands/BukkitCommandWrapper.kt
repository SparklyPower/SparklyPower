package net.perfectdreams.dreamcore.utils.commands

import net.perfectdreams.dreamcore.DreamCore
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginIdentifiableCommand
import org.bukkit.plugin.Plugin

class BukkitCommandWrapper(val _plugin: Plugin, val command: Command<CommandContext>) : org.bukkit.command.Command(
    command.labels.first().split(" ")[0],
    "", // Description (nobody cares)
    "/${ command.labels.first()}", // Usage Message (nobody cares²)
    command.labels.drop(1) // Aliases, vamos retirar a primeira (que é a label) e vlw flw
), PluginIdentifiableCommand {
    val commands = mutableListOf<Command<CommandContext>>()

    init {
        commands.add(command)
    }

    override fun getPlugin() = _plugin

    override fun execute(p0: CommandSender, p1: String, p2: Array<String>): Boolean {
        // Comandos com espaços na label, yeah!
        var valid = false
        var validLabel: String? = null

        val checkArguments = p2.toMutableList()
        val rawArgument0 = checkArguments.getOrNull(0)
        var removeArgumentCount = 0
        var matchedCommand: Command<CommandContext>? = null

        if (commands.size == 1) {
            matchedCommand = commands.first()
            valid = true
        } else {
            for (command in commands) {
                val labels = command.labels

                for (label in labels) {
                    var currentRemoveArgumentCount = 0

                    val subLabels = label.split(" ").toMutableList().drop(1)

                    var validLabelCount = 0

                    for ((index, subLabel) in subLabels.withIndex()) {
                        val rawArgumentAt = checkArguments.getOrNull(index) ?: break

                        if (rawArgumentAt.equals(subLabel, true)) { // ignoreCase = true ~ Permite usar "+cOmAnDo"
                            validLabelCount++
                            currentRemoveArgumentCount++
                        }
                    }

                    if (validLabelCount == subLabels.size) {
                        valid = true
                        validLabel = subLabels.joinToString(" ")
                        matchedCommand = command
                        removeArgumentCount = currentRemoveArgumentCount
                        break
                    }
                }
            }
        }

        if (valid && matchedCommand != null) {
            try {
                val requiredPermission = matchedCommand.permission
                if (requiredPermission != null && !p0.hasPermission(requiredPermission))
                    throw CommandException(DreamCore.dreamConfig.strings.withoutPermission)

                matchedCommand.executor.invoke(
                    CommandContext(
                        p0,
                        p1,
                        matchedCommand,
                        p2.drop(removeArgumentCount).toTypedArray()
                    )
                )
            } catch (e: CommandException) {
                p0.sendMessage(e.reason)
            }
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        val completions = mutableListOf<String>()

        /* val methods = sparklyCommand::class.members

        val currentArgument = args.last()

        for (method in methods.filter { it.findAnnotation<Subcommand>() != null }.sortedByDescending { it.parameters.size }) {
            val annotation = method.findAnnotation<Subcommand>()!!
            for (value in annotation.labels) {
                if (value.startsWith(currentArgument, true))
                    completions.add(value)
            }
        } */

        completions.addAll(super.tabComplete(sender, alias, args))

        return completions
    }
}