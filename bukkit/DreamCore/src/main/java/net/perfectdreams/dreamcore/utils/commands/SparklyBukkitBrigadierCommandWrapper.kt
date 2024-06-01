package net.perfectdreams.dreamcore.utils.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.Suggestions
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import net.perfectdreams.dreamcore.utils.commands.exceptions.CommandException
import net.perfectdreams.dreamcore.utils.commands.options.*
import java.util.concurrent.CompletableFuture

// Once upon a time this was a real Bukkit command
// But after Paper 1.20.6 introduced the Brigadier API, we don't need that anymore
// But we still use this class to convert our declarations to Brigadier
class SparklyBukkitBrigadierCommandWrapper(
    // Because the CommandRegisteredEvent is only triggered once, even if the command has multiple labels
    // We will create a new SparklyBukkitBrigadierCommandWrapper for each label
    // (And there isn't a way to register multiple cmds on a single CommandRegisteredEvent event)
    val label: String,
    val declaration: SparklyCommandDeclaration,
    private val plugin: KotlinPlugin,
    private val sparklyCommandManager: SparklyCommandManager,
) {
    fun convertRootDeclarationToBrigadier(declaration: SparklyCommandDeclaration): LiteralArgumentBuilder<CommandSourceStack> {
        return LiteralArgumentBuilder.literal<CommandSourceStack>(label)
            .apply { transformStuff(declaration, this) }
    }

    fun convertDeclarationToBrigadier(declaration: SparklyCommandDeclaration): List<LiteralArgumentBuilder<CommandSourceStack>> {
        return declaration.labels.map {
            LiteralArgumentBuilder.literal<CommandSourceStack>(it)
                .apply { transformStuff(declaration, this) }
        }
    }

    private fun transformStuff(declaration: SparklyCommandDeclaration, literalArgumentBuilder: LiteralArgumentBuilder<CommandSourceStack>) {
        val executor = com.mojang.brigadier.Command<CommandSourceStack> { commandContext ->
            plugin.logger.fine { "Calling ${declaration}'s executor..." }
            val context = CommandContext(commandContext)

            try {
                val requiredPermissions = mutableListOf<String>()
                val selfPermissions = declaration.permissions
                if (selfPermissions?.isNotEmpty() == true)
                    requiredPermissions.addAll(selfPermissions)
                val rootPermissions = this.declaration.permissions
                if (this.declaration.childrenInheritPermissions && rootPermissions?.isNotEmpty() == true)
                    requiredPermissions.addAll(rootPermissions)

                // If there are permissions set in the declaration, we are going to check with "requirePermissions"
                // If the user does not have a permission, it will fail! (so, it will throw an exception)
                // This needs to be within this try catch block so it will catch the CommandException!
                if (requiredPermissions.isNotEmpty())
                    context.requirePermissions(*requiredPermissions.toTypedArray())

                val executor = declaration.executor ?: error("I couldn't find a executor!")
                executor.execute(context, CommandArguments(context))
            } catch (e: Throwable) {
                if (e is CommandException)
                    context.sendMessage(e.component)
                else
                    e.printStackTrace()
            }

            return@Command com.mojang.brigadier.Command.SINGLE_SUCCESS
        }

        literalArgumentBuilder.apply {
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
                            is WordCommandOption, is OptionalWordCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, String>(
                                it.name,
                                StringArgumentType.word()
                            )

                            // ===[ BOOLEAN ]===
                            is BooleanCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Boolean>(
                                it.name,
                                BoolArgumentType.bool()
                            )

                            // ===[ INTEGER ]===
                            is IntegerCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Int>(
                                it.name,
                                IntegerArgumentType.integer()
                            )
                            is IntegerMinCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Int>(
                                it.name,
                                IntegerArgumentType.integer(it.min)
                            )
                            is IntegerMinMaxCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Int>(
                                it.name,
                                IntegerArgumentType.integer(it.min, it.max)
                            )
                            is OptionalIntegerCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Int>(
                                it.name,
                                IntegerArgumentType.integer()
                            )
                            is OptionalIntegerMinCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Int>(
                                it.name,
                                IntegerArgumentType.integer(it.min)
                            )
                            is OptionalIntegerMinMaxCommandOption -> RequiredArgumentBuilder.argument<CommandSourceStack, Int>(
                                it.name,
                                IntegerArgumentType.integer(it.min, it.max)
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

                                    val completableFuture = CompletableFuture<Suggestions>()

                                    plugin.launchAsyncThread {
                                        it.suggestsBlock.invoke(context, suggestionsBuilder)

                                        completableFuture.complete(suggestionsBuilder.build())
                                    }

                                    return@suggests completableFuture
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
}