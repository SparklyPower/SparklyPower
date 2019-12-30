package net.perfectdreams.dreamcore.utils.commands

import org.bukkit.ChatColor

@Deprecated(message = "Please use the new command framework")
class ExecutedCommandException(val minecraftMessage: String? = null, message: String? = null) : RuntimeException(message ?: ChatColor.stripColor(minecraftMessage))