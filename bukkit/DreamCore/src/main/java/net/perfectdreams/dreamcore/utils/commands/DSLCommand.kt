package net.perfectdreams.dreamcore.utils.commands

import org.bukkit.plugin.Plugin

interface DSLCommandBase<Plugin> {
    fun command(plugin: Plugin): Command<CommandContext>

    fun create(labels: List<String>, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
        return command(
            this::class.simpleName!!,
            labels
        ) {
            builder.invoke(this)
        }
    }
}