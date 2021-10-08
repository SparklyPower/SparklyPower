package net.perfectdreams.dreamcore.utils.commands.declarations

import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration

class SparklyCommandDeclaration(
    val labels: List<String>,
    val permissions: List<String>? = null,
    val executor: SparklyCommandExecutorDeclaration? = null,
    val subcommands: List<SparklyCommandDeclaration>
)