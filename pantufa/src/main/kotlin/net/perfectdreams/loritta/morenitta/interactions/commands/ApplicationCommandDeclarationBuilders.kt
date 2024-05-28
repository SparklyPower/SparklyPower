package net.perfectdreams.loritta.morenitta.interactions.commands

// ===[ SLASH COMMANDS ]===
fun slashCommand(name: String, description: String, block: SlashCommandDeclarationBuilder.() -> (Unit)) = SlashCommandDeclarationBuilder(
    name,
    description
).apply(block)

@InteraKTionsUnleashedDsl
class SlashCommandDeclarationBuilder(
    val name: String,
    val description: String
) {
    var executor: LorittaSlashCommandExecutor? = null
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<SlashCommandGroupDeclarationBuilder>()

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
            subcommands.map { it.build() }
        )
    }
}