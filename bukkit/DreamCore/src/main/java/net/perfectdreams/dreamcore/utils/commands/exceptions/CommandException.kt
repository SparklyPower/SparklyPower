package net.perfectdreams.dreamcore.utils.commands.exceptions

import net.kyori.adventure.text.Component

/**
 * Useful for command control flow, this allows you a quick and easy way to "halt" the execution of an command.
 *
 * Instead of showing the pre-defined generic error message, the [component] message should be sent to the user.
 *
 * Logging the error is not required.
 */
class CommandException(val component: Component) : RuntimeException()