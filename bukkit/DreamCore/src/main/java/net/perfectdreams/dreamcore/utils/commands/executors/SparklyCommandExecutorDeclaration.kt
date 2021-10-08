package net.perfectdreams.dreamcore.utils.commands.executors

import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions

// The "parent" is Any to avoid issues with anonymous classes
// When using anonymous classes, you can use another type to match declarations
open class SparklyCommandExecutorDeclaration(val parent: Any) {
    open val options: CommandOptions = CommandOptions.NO_OPTIONS
}