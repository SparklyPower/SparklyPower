package net.perfectdreams.dreamcore.utils.commands

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.bukkit.BukkitBrigForwardingMap
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import org.bukkit.plugin.Plugin

class SparklyCommandManager(val plugin: KotlinPlugin) {
    val declarations = mutableListOf<SparklyCommandDeclaration>()

    fun register(command: SparklyCommandDeclarationWrapper) {
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

        val manager: LifecycleEventManager<Plugin> = plugin.getLifecycleManager()
        manager.registerEventHandler<ReloadableRegistrarEvent<Commands>>(
            LifecycleEvents.COMMANDS
        ) { event: ReloadableRegistrarEvent<Commands> ->
            // println("LifecycleEvents.COMMANDS for ${plugin.name}")
            val commands: Commands = event.registrar()
            commandWrappers.forEach {
                commands.register(it.convertRootDeclarationToBrigadier(command.declaration()).build() as LiteralCommandNode<CommandSourceStack>)
            }
        }
    }

    // TODO: How to unregister brigadier commands?
}