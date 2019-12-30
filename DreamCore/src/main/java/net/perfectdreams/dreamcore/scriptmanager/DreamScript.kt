package net.perfectdreams.dreamcore.scriptmanager

import com.okkero.skedule.BukkitSchedulerController
import com.okkero.skedule.CoroutineTask
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.RegisteredListener

open class DreamScript : Listener {
	lateinit var fileName: String

	val commands = mutableListOf<AbstractCommand>()
	val tasks = mutableListOf<CoroutineTask>()

	open fun enable() {
	}

	open fun disable() {
	}

	fun enableScript() {
		enable()
		Bukkit.getPluginManager().registerEvents(this, DreamCore.INSTANCE)
	}

	fun disableScript() {
		disable()
		commands.forEach {
			it.unregister()
		}
		commands.clear()
		tasks.forEach {
			it.cancel()
		}
		tasks.clear()
		for (handler in HandlerList.getHandlerLists()) {
			handler.unregister(this)
		}
	}

	fun registerCommand(command: AbstractCommand) {
		command.register()
		commands.add(command)
	}

	fun registerCommand(label: String, aliases: List<String> = listOf(), permission: String? = null, callback: (CommandSender, Array<String>) -> (Unit)) {
		registerCommand(object: AbstractCommand(label) {
			@Subcommand
			fun root(sender: CommandSender, @InjectArgument(ArgumentType.ALL_ARGUMENTS_ARRAY) arguments: Array<String>) {
				callback.invoke(sender, arguments)
			}
		}
		)
	}

	fun schedule(initialContext: SynchronizationContext = SynchronizationContext.SYNC, task: suspend BukkitSchedulerController.() -> Unit) {
		val corotuineTask = Bukkit.getScheduler().schedule(DreamCore.INSTANCE, initialContext, task)
		tasks.add(corotuineTask)
	}
}