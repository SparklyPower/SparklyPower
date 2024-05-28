package net.perfectdreams.loritta.morenitta.interactions.commands

data class SlashCommandDeclaration(
    val name: String,
    val description: String,
    val executor: LorittaSlashCommandExecutor?,
    val subcommands: List<SlashCommandDeclaration>,
    val subcommandGroups: List<SlashCommandGroupDeclaration>
)

data class SlashCommandGroupDeclaration(
    val name: String,
    val description: String,
    val subcommands: List<SlashCommandDeclaration>
)