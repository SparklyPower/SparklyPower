package net.perfectdreams.dreamcore.utils.commands

import org.bukkit.command.CommandSender

object CommandManager {
	val argumentContexts = mutableListOf<ArgumentContextWrapper>()
	val contexts = mutableListOf<SenderContextWrapper>()

	fun <T> registerArgumentContext(clazz: Class<T>, callback: (CommandSender, String) -> (Any?)) {
		argumentContexts.add(ArgumentContextWrapper(clazz, null, callback))
	}

	fun <T> registerSenderContext(clazz: Class<T>, callback: (CommandSender) -> (Any?)) {
		contexts.add(SenderContextWrapper(clazz, null, callback))
	}

	fun <T> registerArgumentContext(clazz: Class<T>, name: String, callback: (CommandSender, String) -> (Any?)) {
		argumentContexts.add(ArgumentContextWrapper(clazz, null, callback))
	}

	fun <T> registerSenderContext(clazz: Class<T>, name: String, callback: (CommandSender) -> (Any?)) {
		contexts.add(SenderContextWrapper(clazz, null, callback))
	}

	class ArgumentContextWrapper(val clazz: Class<*>, val name: String?, val callback: (CommandSender, String) -> Any?)
	class SenderContextWrapper(val clazz: Class<*>, val name: String?, val callback: (CommandSender) -> Any?)
}