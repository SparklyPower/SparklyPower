package net.perfectdreams.dreamcore.utils.commands.executors

import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext

/**
 * This is the class that should be inherited if you
 * want to create an Interaction Command.
 */
abstract class SparklyCommandExecutor {
    abstract fun execute(context: CommandContext, args: CommandArguments)

    /**
     * Used by the [net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration] to match declarations to executors.
     *
     * By default the class of the executor is used, but this may cause issues when using anonymous classes!
     *
     * To avoid this issue, you can replace the signature with another unique identifier
     */
    open fun signature(): Any = this::class
}