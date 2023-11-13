package net.perfectdreams.dreamcore.utils.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor

class SparklyCommandDeclaration(
    val labels: List<String>,
    val permissions: List<String>? = null,
    val executor: SparklyCommandExecutor? = null,
    val subcommands: List<SparklyCommandDeclaration>,
    val childrenInheritPermissions: Boolean
)