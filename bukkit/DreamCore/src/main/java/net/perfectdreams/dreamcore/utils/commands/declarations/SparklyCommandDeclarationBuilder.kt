package net.perfectdreams.dreamcore.utils.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration

fun sparklyCommand(labels: List<String>, block: SparklyCommandDeclarationBuilder.() -> (Unit)): SparklyCommandDeclaration {
    return SparklyCommandDeclarationBuilder(labels)
        .apply(block)
        .build()
}

class SparklyCommandDeclarationBuilder(val labels: List<String>) {
    var executor: SparklyCommandExecutorDeclaration? = null
    var permissions: List<String>? = null
    val subcommands = mutableListOf<SparklyCommandDeclaration>()

    fun subcommand(labels: List<String>, block: SparklyCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SparklyCommandDeclarationBuilder(labels).apply(block)
            .build()
    }

    fun build(): SparklyCommandDeclaration {
        return SparklyCommandDeclaration(
            labels,
            permissions,
            executor,
            subcommands
        )
    }
}