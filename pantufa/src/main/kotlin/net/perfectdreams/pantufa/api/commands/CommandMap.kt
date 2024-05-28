package net.perfectdreams.pantufa.api.commands

interface CommandMap<Cmd: Command<CommandContext>> {
	fun register(command: Cmd)

	fun registerAll(vararg commands: Cmd) {
		commands.forEach { register(it) }
	}

	fun unregister(command: Cmd)

	fun unregisterAll(vararg commands: Cmd) {
		commands.forEach { unregister(it) }
	}
}