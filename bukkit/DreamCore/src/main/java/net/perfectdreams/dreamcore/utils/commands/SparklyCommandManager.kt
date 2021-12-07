package net.perfectdreams.dreamcore.utils.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import me.lucko.commodore.CommodoreProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.exceptions.CommandException
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandMap
import org.bukkit.craftbukkit.v1_18_R1.CraftServer
import org.bukkit.plugin.Plugin
import java.lang.Compiler.command

class SparklyCommandManager(val plugin: Plugin) {
    val declarations = mutableListOf<SparklyCommandDeclaration>()
    val executors = mutableListOf<SparklyCommandExecutor>()
    // val commandWrappers = mutableListOf<SparklyBukkitCommandWrapper>()

    private fun getCommandMap(): CommandMap {
        return Bukkit.getCommandMap()
    }

    fun register(command: SparklyCommandDeclarationWrapper, vararg executors: SparklyCommandExecutor) {
        // https://github.com/MrIvanPlays/CommandWorker/blob/master/bukkit/cmdregistry/v1_16_R2/src/main/java/com/mrivanplays/commandworker/bukkit/registry/CmdRegistry1_16_R2.java
        // Get a commodore instance, used to register Brigadier commands
        val commodore = CommodoreProvider.getCommodore(plugin)

        val declaration = command.declaration()
        plugin.logger.info { "Registering ${declaration.labels}..." }

        this.declarations.add(declaration)
        this.executors.addAll(executors)

        val commands = convertDeclarationToBrigadier(declaration)

        for (brigadierCommand in commands) {
            commodore.register(brigadierCommand as LiteralArgumentBuilder<CommandSourceStack>) // TODO: Remove this cast
        }
    }

    // TODO: How to unregister brigadier commands?

    private fun convertDeclarationToBrigadier(declaration: SparklyCommandDeclaration): List<ArgumentBuilder<CommandSourceStack, *>> {
        val commands = mutableListOf<ArgumentBuilder<CommandSourceStack, *>>()

        val executor = Command<CommandSourceStack> { commandContext ->
            plugin.logger.fine { "Calling ${declaration}'s executor..." }
            val context = CommandContext(commandContext)

            try {
                // If there are permissions set in the declaration, we are going to check with "requirePermissions"
                // If the user does not have a permission, it will fail! (so, it will throw an exception)
                // This needs to be within this try catch block so it will catch the CommandException!
                if (declaration.permissions?.isNotEmpty() == true)
                    context.requirePermissions(*declaration.permissions.toTypedArray())

                val executor = executors.firstOrNull { it.signature() == declaration.executor?.parent } ?: error("I couldn't find a executor for ${declaration.executor?.parent}! Did you forget to register the executor?")
                executor.execute(context, CommandArguments(context))
            } catch (e: Throwable) {
                if (e is CommandException)
                    context.sendMessage(e.component)
                else
                    e.printStackTrace()
            }

            return@Command SINGLE_SUCCESS
        }

        for (label in declaration.labels) {
            commands += LiteralArgumentBuilder.literal<CommandSourceStack>(label)
                .apply {
                    declaration.subcommands.forEach {
                        convertDeclarationToBrigadier(it).forEach {
                            then(it)
                        }
                    }

                    if (declaration.executor != null) {
                        if (declaration.executor.options.arguments.isEmpty()) {
                            executes(executor)
                        } else {
                            val arguments = declaration.executor.options.arguments.map {
                                when (it) {
                                    // ===[ STRING ]===
                                    is GreedyStringCommandOption, is OptionalGreedyStringCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, String>(
                                        it.name,
                                        StringArgumentType.greedyString()
                                    )
                                    is QuotableStringCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, String>(
                                        it.name,
                                        StringArgumentType.string()
                                    )
                                    is WordCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, String>(
                                        it.name,
                                        StringArgumentType.word()
                                    )

                                    // ===[ BOOLEAN ]===
                                    is BooleanCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Boolean>(
                                        it.name,
                                        BoolArgumentType.bool()
                                    )

                                    // ===[ DOUBLE ]===
                                    is DoubleCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Double>(
                                        it.name,
                                        DoubleArgumentType.doubleArg()
                                    )
                                    is DoubleMinCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Double>(
                                        it.name,
                                        DoubleArgumentType.doubleArg(it.min)
                                    )
                                    is DoubleMinMaxCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Double>(
                                        it.name,
                                        DoubleArgumentType.doubleArg(it.min, it.max)
                                    )

                                    // ===[ PLAYER ]===
                                    is PlayerCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, EntitySelector>(
                                        it.name,
                                        EntityArgument.player()
                                    ) // Single target, players only
                                }.apply {
                                    if (it.suggestsBlock != null)
                                        this.suggests { commandContext, suggestionsBuilder ->
                                            val context = CommandContext(commandContext)
                                            // context.sendMessage("asked for suggestions owo")
                                            it.suggestsBlock.invoke(context, suggestionsBuilder)
                                            suggestionsBuilder.buildFuture()
                                        }
                                }
                            }

                            // We will reverse the list, and we also will include this node
                            val nodes = mutableListOf<ArgumentBuilder<CommandSourceStack, *>>(this).apply {
                                this.addAll(arguments)
                            }

                            // To work with optionals, we need to find the first argument that is optional!
                            var applyExecutorsFrom = declaration.executor.options.arguments.indexOfFirst { it.optional }
                            if (applyExecutorsFrom == -1) // If all of them are -1, we are going to apply to the last
                                applyExecutorsFrom = declaration.executor.options.arguments.size // No need to subtract -1, because we have the source node here too!

                            plugin.logger.fine { "Executors will be applied from $applyExecutorsFrom to ${declaration.executor.options.arguments.size}" }
                            for (i in applyExecutorsFrom..declaration.executor.options.arguments.size) {
                                plugin.logger.fine { "Applying executor to node $i ${nodes[i]}" }
                                nodes[i].executes(executor)
                            }

                            // This will go from the first of the list down to the penultimate entry and apply a "then"
                            for (x in (nodes.size - 1) downTo 1) {
                                val first = nodes[x]
                                val last = nodes[x - 1]

                                last.then(first)
                            }
                        }
                    }
                }
        }

        return commands
    }
}