package net.perfectdreams.dreamcore.utils.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

fun sparklyCommand(labels: List<String>, block: SparklyCommandDeclarationBuilder.() -> (Unit)): SparklyCommandDeclaration {
    return SparklyCommandDeclarationBuilder(labels)
        .apply(block)
        .build()
}

class SparklyCommandDeclarationBuilder(val labels: List<String>) {
    var executor: SparklyCommandExecutor? = null
    var permissions: List<String>? = null
    val subcommands = mutableListOf<SparklyCommandDeclaration>()
    var permission: String
        get() = TODO("Can't get single permission, please use permissions")
        set(value) {
            permissions = listOf(value)
        }

    /**
     * If disabled, children commands won't inherit the permission set in the root command declaration
     *
     * **Only permissions at the root level will be inherited!** Nested permissions won't be inherited.
     */
    var childrenInheritPermissions = true

    fun subcommand(labels: List<String>, block: SparklyCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SparklyCommandDeclarationBuilder(labels).apply(block)
            .build()
    }

    fun build(): SparklyCommandDeclaration {
        return SparklyCommandDeclaration(
            labels,
            permissions,
            executor,
            subcommands,
            childrenInheritPermissions
        )
    }
}