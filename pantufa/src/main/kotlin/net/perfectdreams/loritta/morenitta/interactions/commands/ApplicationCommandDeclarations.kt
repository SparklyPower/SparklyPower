package net.perfectdreams.loritta.morenitta.interactions.commands

import net.perfectdreams.loritta.common.commands.CommandCategory

data class SlashCommandDeclaration(
    val name: String,
    val description: String,
    val category: CommandCategory,
    val examples: List<String>,
    var requireMinecraftAccount: Boolean,
    var enableLegacyMessageSupport: Boolean,
    var alternativeLegacyLabels: List<String>,
    var alternativeLegacyAbsoluteCommandPaths: List<String>,
    val executor: LorittaSlashCommandExecutor?,
    val subcommands: List<SlashCommandDeclaration>,
    val subcommandGroups: List<SlashCommandGroupDeclaration>
)

data class SlashCommandGroupDeclaration(
    val name: String,
    val description: String,
    var alternativeLegacyLabels: List<String>,
    val subcommands: List<SlashCommandDeclaration>
)