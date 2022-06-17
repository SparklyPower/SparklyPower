package net.perfectdreams.dreamcore.utils.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.Style
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclaration
import net.perfectdreams.dreamcore.utils.commands.exceptions.CommandException
import net.perfectdreams.dreamcore.utils.commands.options.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginIdentifiableCommand
import org.bukkit.craftbukkit.v1_19_R1.command.VanillaCommandWrapper
import java.util.concurrent.CompletableFuture

class SparklyBukkitBrigadierCommandWrapper(
    labels: List<String>,
    val declaration: SparklyCommandDeclaration,
    private val plugin: KotlinPlugin,
    private val sparklyCommandManager: SparklyCommandManager,
) : Command(labels[0], "", "/${labels[0]}", labels.drop(1)), PluginIdentifiableCommand {
    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        // We can't register the Brigadier command directly to the vanilla's command map, because this has a lot of disadvantages:
        // * The permission is "minecraft.labelname", which is undesirable for us
        // * The command is registered in the "minecraft" namespace
        // So we are going to workaround this: We are going to create our own CommandDispatcher, register the Brigadier command to it and then execute it!
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val brigadierDeclaration = convertDeclarationToBrigadier(declaration)
        dispatcher.register(brigadierDeclaration)

        // Is this correct? I don't think so, but let's see what happens
        // The "convertDeclarationToBrigadier" result always uses this command "label" argument, that's why we aren't using the "commandLabel" argument here!
        val commandWithArguments = listOf(label, *args)
        val commandListenerWrapper = VanillaCommandWrapper.getListener(sender)
        try {
            val r = dispatcher.execute((commandWithArguments.joinToString(" ")), commandListenerWrapper)
        } catch (commandSyntaxException: CommandSyntaxException) {
            // From Minecraft's Commands#performCommand function
            commandListenerWrapper.sendFailure(ComponentUtils.fromMessage(commandSyntaxException.rawMessage))
            if (commandSyntaxException.input != null && commandSyntaxException.cursor >= 0) {
                val j: Int = Math.min(commandSyntaxException.input.length, commandSyntaxException.cursor)
                val ichatmutablecomponent = Component.literal("").withStyle(ChatFormatting.GRAY).withStyle { chatmodifier: Style ->
                        chatmodifier.withClickEvent(
                            ClickEvent(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                label
                            )
                        ) // CraftBukkit
                    }
                if (j > 10) {
                    ichatmutablecomponent.append("...")
                }
                ichatmutablecomponent.append(commandSyntaxException.getInput().substring(Math.max(0, j - 10), j))
                if (j < commandSyntaxException.getInput().length) {
                    val ichatmutablecomponent1 = Component.literal(commandSyntaxException.getInput().substring(j))
                        .withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE)
                    ichatmutablecomponent.append(ichatmutablecomponent1 as Component)
                }
                ichatmutablecomponent.append(
                    Component.translatable("command.context.here")
                        .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC) as Component
                )
                commandListenerWrapper.sendFailure(ichatmutablecomponent)
            }
        }
        return true
    }

    override fun getPlugin() = plugin

    fun convertDeclarationToBrigadier(declaration: SparklyCommandDeclaration): LiteralArgumentBuilder<CommandSourceStack> {
        val executor = com.mojang.brigadier.Command<CommandSourceStack> { commandContext ->
            plugin.logger.fine { "Calling ${declaration}'s executor..." }
            val context = CommandContext(commandContext)

            try {
                // If there are permissions set in the declaration, we are going to check with "requirePermissions"
                // If the user does not have a permission, it will fail! (so, it will throw an exception)
                // This needs to be within this try catch block so it will catch the CommandException!
                if (declaration.permissions?.isNotEmpty() == true)
                    context.requirePermissions(*declaration.permissions.toTypedArray())

                val executor = sparklyCommandManager.executors.firstOrNull { it.signature() == declaration.executor?.parent } ?: error("I couldn't find a executor for ${declaration.executor?.parent}! Did you forget to register the executor?")
                executor.execute(context, CommandArguments(context))
            } catch (e: Throwable) {
                if (e is CommandException)
                    context.sendMessage(e.component)
                else
                    e.printStackTrace()
            }

            return@Command com.mojang.brigadier.Command.SINGLE_SUCCESS
        }

        val literalArgumentBuilder = LiteralArgumentBuilder.literal<CommandSourceStack>(declaration.labels.first())
            .apply {
                declaration.subcommands.forEach {
                    then(convertDeclarationToBrigadier(it))
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

        return literalArgumentBuilder
    }
}