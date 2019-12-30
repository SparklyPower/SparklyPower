package net.perfectdreams.commands.bukkit

import org.bukkit.ChatColor

class ExecutedCommandException(val minecraftMessage: String? = null, message: String? = null) : RuntimeException(message ?: ChatColor.stripColor(minecraftMessage))