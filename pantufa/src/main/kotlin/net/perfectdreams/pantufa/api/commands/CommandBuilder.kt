package net.perfectdreams.pantufa.api.commands

import net.perfectdreams.pantufa.PantufaBot

fun command(loritta: PantufaBot, commandName: String, labels: List<String>, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
	val b = CommandBuilder<CommandContext>(loritta, commandName, labels)
	builder.invoke(b)
	return b.build()
}

open class CommandBuilder<context : CommandContext>(
		val pantufa: PantufaBot,
		val commandName: String,
		val labels: List<String>
) {
	var executeCallback: (suspend context.() -> (Unit))? = null

	fun executes(callback: suspend context.() -> (Unit)) {
		this.executeCallback = callback
	}

	fun build(): Command<context> {
		return Command(
				pantufa = pantufa,
				labels = labels,
				commandName = commandName,
				executor = executeCallback!!
		)
	}
}