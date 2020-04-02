package net.perfectdreams.dreamcore.utils.commands

fun command(commandName: String, labels: List<String>, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
	val b = CommandBuilder<CommandContext>(commandName, labels)
	builder.invoke(b)
	return b.build()
}

open class CommandBuilder<context : CommandContext>(
		val commandName: String,
		val labels: List<String>
) {
	var executeCallback: (context.() -> (Unit))? = null
	var permission: String? = null

	fun executes(callback: context.() -> (Unit)) {
		this.executeCallback = callback
	}

	fun build(): Command<context> {
		val _permission = permission
		return Command<context>(
				labels = labels,
				commandName = commandName,
				executor = executeCallback!!
		).apply {
			this.permission = _permission
		}
	}
}