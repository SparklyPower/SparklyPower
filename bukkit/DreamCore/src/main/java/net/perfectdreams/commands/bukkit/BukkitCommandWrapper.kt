package net.perfectdreams.commands.bukkit

import net.perfectdreams.commands.annotation.Subcommand
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginIdentifiableCommand
import org.bukkit.plugin.Plugin
import kotlin.reflect.full.findAnnotation

class BukkitCommandWrapper(val commandManager: BukkitCommandManager, val sparklyCommand: SparklyCommand) : Command(
		sparklyCommand.labels.first(), // Label
		"", // Description (nobody cares)
		"/${sparklyCommand.labels.first()}", // Usage Message (nobody cares²)
		sparklyCommand.labels.drop(0) // Aliases, vamos retirar a primeira (que é a label) e vlw flw
), PluginIdentifiableCommand {
	override fun getPlugin(): Plugin {
		return commandManager.plugin
	}

	override fun execute(p0: CommandSender, p1: String, p2: Array<String>): Boolean {
		commandManager.dispatchBlocking(p0, sparklyCommand, p1, p2)
		return true
	}

	override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
		val completions = mutableListOf<String>()

		val methods = sparklyCommand::class.members

		val currentArgument = args.last()

		for (method in methods.filter { it.findAnnotation<Subcommand>() != null }.sortedByDescending { it.parameters.size }) {
			val annotation = method.findAnnotation<Subcommand>()!!
			for (value in annotation.labels) {
				if (value.startsWith(currentArgument, true))
					completions.add(value)
			}
		}

		completions.addAll(super.tabComplete(sender, alias, args))

		return completions
	}
}