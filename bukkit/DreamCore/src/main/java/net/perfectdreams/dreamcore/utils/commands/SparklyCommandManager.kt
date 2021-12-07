package net.perfectdreams.dreamcore.utils.commands

import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.registerEvents
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class SparklyCommandManager(val plugin: Plugin) {
    val declarations = mutableListOf<SparklyCommandDeclaration>()
    val executors = mutableListOf<SparklyCommandExecutor>()
    var hasCommandListenerRegistered = false

    fun register(command: SparklyCommandDeclarationWrapper, vararg executors: SparklyCommandExecutor) {
        // If we didn't register the command listener yet, then let's register it now!
        if (!hasCommandListenerRegistered) {
            plugin.logger.info { "Registering CommandListener for $plugin because it wasn't registered before..." }
            plugin.registerEvents(CommandListener(this))
            hasCommandListenerRegistered = true
        }

        val declaration = command.declaration()
        plugin.logger.info { "Registering ${declaration.labels}..." }
        this.declarations.add(declaration)
        this.executors.addAll(executors)

        val commandWrapper = SparklyBukkitBrigadierCommandWrapper(
            declaration.labels,
            declaration,
            plugin,
            this
        )

        Bukkit.getCommandMap().register(plugin.name.lowercase(), commandWrapper)
    }

    // TODO: How to unregister brigadier commands?
}