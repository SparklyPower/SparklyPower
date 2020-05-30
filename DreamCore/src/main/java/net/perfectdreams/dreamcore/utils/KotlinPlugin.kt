package net.perfectdreams.dreamcore.utils

import co.aikar.commands.BaseCommand
import co.aikar.commands.PaperCommandManager
import net.perfectdreams.commands.bukkit.BukkitCommandManager
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.commands.*
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

/**
 * Classe que permite "reload" sem explodir o servidor
 *
 * Todos os plugins do PerfectDreams devem extender a classe KotlinPlugin!
 */
open class KotlinPlugin : JavaPlugin() {
	// Lista de comandos registrados por este plugin
	val commandList = mutableListOf<AbstractCommand>()
	@Deprecated("Please use dreamCommandManager")
	val commandManager by lazy { PaperCommandManager(this) }
	@Deprecated("Please use dreamCommandManager")
	val bukkitCommandManager by lazy { BukkitCommandManager(this) }
	val dreamCommandManager by lazy { DreamCommandManager(this) }
	val serverEvents = mutableListOf<ServerEvent>()

	override fun onEnable() {
		softEnable()
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

		// Problema resolvido!
	}

	open fun softEnable() {

	}

	open fun softDisable() {

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
	 *
	 * @param command comando
	 */
	@Deprecated(message = "Usar command DSL")
	fun registerCommand(command: BaseCommand) {
		commandManager.registerCommand(command)
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
}