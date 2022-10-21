package net.perfectdreams.dreamcore.utils

import kotlinx.coroutines.*
import net.perfectdreams.commands.bukkit.BukkitCommandManager
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.commands.*
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.scheduler.BukkitDispatcher
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

/**
 * Classe que permite "reload" sem explodir o servidor
 *
 * Todos os plugins do PerfectDreams devem extender a classe KotlinPlugin!
 */
open class KotlinPlugin : JavaPlugin() {
	companion object {
		internal val PLUGIN_TASK_THREAD_LOCAL = ThreadLocal<KotlinPlugin>()
	}

	// Lista de comandos registrados por este plugin
	val commandList = mutableListOf<AbstractCommand>()
	@Deprecated("Please use dreamCommandManager")
	val bukkitCommandManager by lazy { BukkitCommandManager(this) }
	val dreamCommandManager by lazy { DreamCommandManager(this) }
	val sparklyCommandManager by lazy { SparklyCommandManager(this) }
	val serverEvents = mutableListOf<ServerEvent>()
	private val activeJobs = ConcurrentLinkedQueue<Job>()
	private val recipes = mutableListOf<NamespacedKey>()
	private val pendingTasks = ConcurrentLinkedQueue<Job>()

	override fun onEnable() {
		softEnable()

		Bukkit.getScheduler().runTask(
			this,
			Runnable {
				pendingTasks.forEach { it.start() }
				pendingTasks.clear()
			}
		)
	}

	override fun onDisable() {
		softDisable()

		// Primeiro nós iremos desregistrar todos os comandos deste plugin
		commandList.forEach {
			it.unregister()
		}

		dreamCommandManager.unregisterAll()
		bukkitCommandManager.unregisterAllCommands()

		// E depois nós iremos desregistrar todos os eventos ao desligar
		HandlerList.getHandlerLists().forEach{
			it.unregister(this)
		}

		for (serverEvent in serverEvents.toList())
			unregisterServerEvent(serverEvent)

		recipes.forEach {
			Bukkit.removeRecipe(it)
		}
		recipes.clear()

		activeJobs.forEach {
			it.cancel()
		}
		activeJobs.clear()
		// Problema resolvido!
	}

	open fun softEnable() {

	}

	open fun softDisable() {

	}

	fun launchMainThread(block: suspend CoroutineScope.() -> Unit): Job {
		val job = GlobalScope.launch(
			BukkitDispatcher(this, false) + PLUGIN_TASK_THREAD_LOCAL.asContextElement(value = this@KotlinPlugin),
			block = block
		)

		// Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
		// added to the list, causing leaks.
		// invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
		activeJobs.add(job)
		job.invokeOnCompletion {
			activeJobs.remove(job)
		}

		return job
	}

	fun <T> launchMainThreadDeferred(block: suspend CoroutineScope.() -> T): Deferred<T> {
		val job = GlobalScope.async(
			BukkitDispatcher(this, false) + PLUGIN_TASK_THREAD_LOCAL.asContextElement(value = this@KotlinPlugin),
			block = block
		)
		// Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
		// added to the list, causing leaks.
		// invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
		activeJobs.add(job)
		job.invokeOnCompletion {
			activeJobs.remove(job)
		}

		return job
	}
	
	// We don't use Bukkit's "runTaskAsync" because it queues the task to be executed one tick later, because Bukkit's scheduler only runs every 50ms (one tick)
	fun launchAsyncThread(block: suspend CoroutineScope.() -> Unit): Job = launchCoroutineOnContext(Dispatchers.IO, block)
		.also {
			it.invokeOnCompletion { cause ->
				if (cause is Throwable && cause !is CancellationException)
					logger.log(Level.WARNING, cause) { "Something went wrong while executing async job!" }
			}
		}

	fun <T> launchAsyncThreadDeferred(block: suspend CoroutineScope.() -> T) = launchCoroutineOnContext(Dispatchers.IO, block)

	private fun <T> launchCoroutineOnContext(context: CoroutineContext, block: suspend CoroutineScope.() -> T): Deferred<T> {
		val job = GlobalScope.async(
			context + PLUGIN_TASK_THREAD_LOCAL.asContextElement(value = this@KotlinPlugin),
			block = block
		)

		// Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
		// added to the list, causing leaks.
		// invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
		activeJobs.add(job)
		job.invokeOnCompletion {
			activeJobs.remove(job)
		}

		return job
	}

	/**
	 * Registra um comando
	 */
	@Deprecated(message = "Usar command DSL")
	fun registerCommand(command: AbstractCommand) {
		command.register()
		commandList.add(command)
	}

	/**
	 * Registra um comando
	 */
	@Deprecated(message = "Usar command DSL")
	fun registerCommand(command: SparklyCommand) {
		bukkitCommandManager.registerCommand(command)
	}

	fun registerCommand(command: Command<CommandContext>) {
		dreamCommandManager.registerCommand(command)
	}

	fun <Plugin> registerCommand(command: DSLCommandBase<Plugin>) {
		dreamCommandManager.registerCommand(command.command(this as Plugin))
	}

	fun registerCommand(command: SparklyCommandDeclarationWrapper) {
		sparklyCommandManager.register(command)
	}

	/**
	 * Registra um evento do servidor
	 */
	fun registerServerEvent(event: ServerEvent) {
		serverEvents.add(event)
		DreamCore.INSTANCE.dreamEventManager.events.add(event)
	}

	/**
	 * Remove um evento do servidor dos já registrados
	 */
	fun unregisterServerEvent(event: ServerEvent) {
		serverEvents.remove(event)
		DreamCore.INSTANCE.dreamEventManager.events.remove(event)
	}

	fun addRecipe(name: String, item: ItemStack, shape: List<String>, ingredients: (ShapedRecipe) -> (Unit)): Recipe {
		// create a NamespacedKey for your recipe
		val key = NamespacedKey(this, name)

		return addRecipe(key, item, shape, ingredients)
	}

	fun addRecipe(key: NamespacedKey, item: ItemStack, shape: List<String>, ingredients: (ShapedRecipe) -> (Unit)): Recipe {
		// Create our custom recipe variable
		val recipe = ShapedRecipe(key, item)

		// Here we will set the places. E and S can represent anything, and the letters can be anything. Beware; this is case sensitive.
		recipe.shape(*shape.toTypedArray())

		// Set what the letters represent.
		// E = Emerald, S = Stick
		ingredients.invoke(recipe)

		return addRecipe(key, recipe)
	}

	fun addRecipe(key: NamespacedKey, recipe: Recipe): Recipe {
		recipes += key
		Bukkit.addRecipe(recipe)
		return recipe
	}
}