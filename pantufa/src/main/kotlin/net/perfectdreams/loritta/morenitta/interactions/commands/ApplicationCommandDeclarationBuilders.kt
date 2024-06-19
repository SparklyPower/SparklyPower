package net.perfectdreams.loritta.morenitta.interactions.commands

import net.perfectdreams.loritta.common.commands.CommandCategory

// ===[ SLASH COMMANDS ]===
fun slashCommand(name: String, description: String, category: CommandCategory = CommandCategory.MAGIC, block: SlashCommandDeclarationBuilder.() -> (Unit)) = SlashCommandDeclarationBuilder(
    name,
    description,
    category
).apply(block)

@InteraKTionsUnleashedDsl
class SlashCommandDeclarationBuilder(
    val name: String,
    val description: String,
    val category: CommandCategory = CommandCategory.MAGIC
) {
    var examples: List<String> = emptyList()
    var executor: LorittaSlashCommandExecutor? = null
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    var requireMinecraftAccount: Boolean = false
    val subcommandGroups = mutableListOf<SlashCommandGroupDeclarationBuilder>()
    var enableLegacyMessageSupport = false
    var alternativeLegacyLabels = mutableListOf<String>()
    var alternativeLegacyAbsoluteCommandPaths = mutableListOf<String>()

    fun subcommand(name: String, description: String, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands.add(
            SlashCommandDeclarationBuilder(
                name,
                description
            ).apply(block)
        )
    }

    fun subcommandGroup(name: String, description: String, block: SlashCommandGroupDeclarationBuilder.() -> (Unit)) {
        subcommandGroups.add(
            SlashCommandGroupDeclarationBuilder(
                name,
                description
            ).apply(block)
        )
    }

    fun build(): SlashCommandDeclaration {
        return SlashCommandDeclaration(
            name,
            description,
            category,
            examples,
            requireMinecraftAccount,
            enableLegacyMessageSupport,
            alternativeLegacyLabels,
            alternativeLegacyAbsoluteCommandPaths,
            executor,
            subcommands.map { it.build() },
            subcommandGroups.map { it.build() }
        )
    }
}

@InteraKTionsUnleashedDsl
class SlashCommandGroupDeclarationBuilder(
    val name: String,
    val description: String
) {
    // Groups can't have executors!
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    var alternativeLegacyLabels = mutableListOf<String>()

    fun subcommand(name: String, description: String, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SlashCommandDeclarationBuilder(
            name,
            description
        ).apply(block)
    }

    fun build(): SlashCommandGroupDeclaration {
        return SlashCommandGroupDeclaration(
            name,
            description,
            alternativeLegacyLabels,
            subcommands.map { it.build() }
        )
    }
}