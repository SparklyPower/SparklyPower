package net.perfectdreams.dreamcore.utils.commands.context

import com.mojang.brigadier.context.CommandContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.commands.CommandListenerWrapper
import net.perfectdreams.dreamcore.utils.commands.exceptions.CommandException
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

class CommandContext(val nmsContext: CommandContext<CommandListenerWrapper>) {
    companion object {
        val MISSING_PERMISSIONS: () -> (Component) = {
            Component.text {
                it.color(TextColor.color(255, 0, 0))
                it.content("Você não tem permissão para fazer isso! Xô, sai daqui!!")
            }
        }
    }

    val sender = nmsContext.source.bukkitSender

    fun sendMessage(message: String) = sender.sendMessage(message)
    fun sendMessage(component: Component) = sender.sendMessage(component)
    fun sendMessage(componentBuilder: () -> Component) = sender.sendMessage(componentBuilder)

    /**
     * Requires that the [sender] is a Player. If it isn't, the command will [fail]~.
     */
    fun requirePlayer(): Player {
        val s = sender
        if (s !is Player)
            fail(
                Component.text {
                    it.color(TextColor.color(255, 0, 0))
                    it.content("Apenas jogadores podem executar este comando!")
                }
            )
        return s
    }

    /**
     * Requires that the [sender] is a Console. If it isn't, the command will [fail]~.
     */
    fun requireConsole(): ConsoleCommandSender {
        val s = sender
        if (s !is ConsoleCommandSender)
            fail(
                Component.text {
                    it.color(TextColor.color(255, 0, 0))
                    it.content("Apenas o console pode executar este comando!")
                }
            )
        return s
    }

    /**
     * Requires that the [sender] has all the required [permissions]. If it isn't, the command will [fail]~.
     */
    fun requirePermissions(vararg permissions: String, reason: () -> (Component) = MISSING_PERMISSIONS): Boolean {
        val s = sender
        val hasAllPermissions = permissions.all { s.hasPermission(it) }
        if (!hasAllPermissions)
            fail(reason.invoke())
        return hasAllPermissions
    }

    fun fail(message: String): Nothing = fail(
        net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
            .deserialize(message)
    )

    fun fail(component: Component): Nothing = throw CommandException(component)
}