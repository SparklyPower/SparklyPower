package net.perfectdreams.dreamcore.utils.commands

import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class SparklyCommandManager(val plugin: KotlinPlugin) {
    val declarations = mutableListOf<SparklyCommandDeclaration>()
    var hasCommandListenerRegistered = false

    fun register(command: SparklyCommandDeclarationWrapper) {
        // If we didn't register the command listener yet, then let's register it now!
        if (!hasCommandListenerRegistered) {
            plugin.logger.info { "Registering CommandListener for $plugin because it wasn't registered before..." }
            plugin.registerEvents(CommandListener(this))
            hasCommandListenerRegistered = true
        }

        val declaration = command.declaration()
        plugin.logger.info { "Registering ${declaration.labels}..." }
        this.declarations.add(declaration)

        val commandWrappers = declaration.labels.map {
            SparklyBukkitBrigadierCommandWrapper(
                it,
                declaration,
                plugin,
                this
            )
        }

        commandWrappers.forEach {
            Bukkit.getCommandMap().register(plugin.name.lowercase(), it)
        }
    }

    // TODO: How to unregister brigadier commands?
}