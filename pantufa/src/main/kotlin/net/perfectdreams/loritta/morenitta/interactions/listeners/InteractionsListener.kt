package net.perfectdreams.loritta.morenitta.interactions.listeners

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.updateCommands
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedComponentId
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete.AutocompleteExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.options.*
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.exceptions.CommandException
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.api.commands.styled
import org.jetbrains.exposed.sql.transactions.transaction

class InteractionsListener(private val pantufa: PantufaBot) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val manager = UnleashedCommandManager(pantufa)

    override fun onReady(event: ReadyEvent) {
        if (!pantufa.config.discordInteractions.registerGlobally) {
            pantufa.launch {
                event.jda.guilds.filter { it.idLong in pantufa.config.discordInteractions.guildsToBeRegistered }
                    .forEach {
                        updateCommands(
                            it.idLong
                        ) { commands ->
                            event.jda.updateCommands {
                                addCommands(*commands.toTypedArray())
                            }.complete()
                        }
                    }
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        pantufa.launchMessageJob(event) {
            var rootDeclaration: SlashCommandDeclaration? = null
            var slashDeclaration: SlashCommandDeclaration? = null

            for (declaration in manager.slashCommands) {
                val rootLabel = event.name
                val subcommandGroupLabel = event.subcommandGroup
                val subcommandLabel = event.subcommandName

                if (rootLabel == declaration.name) {
                    if (subcommandGroupLabel == null && subcommandLabel == null) {
                        // Already found it, yay!
                        slashDeclaration = declaration
                        rootDeclaration = declaration
                    } else {
                        // Check root subcommands
                        if (subcommandLabel != null) {
                            if (subcommandGroupLabel == null) {
                                // "/name subcommand"
                                slashDeclaration =
                                    declaration.subcommands.firstOrNull { it.name == subcommandLabel }
                                rootDeclaration = declaration
                                break
                            } else {
                                // "/name subcommandGroup subcommand"
                                slashDeclaration = declaration.subcommandGroups.firstOrNull {
                                    it.name == subcommandGroupLabel
                                }
                                    ?.subcommands
                                    ?.firstOrNull {
                                        it.name == subcommandLabel
                                    }
                                rootDeclaration = declaration
                                break
                            }
                        }
                    }
                    break
                }
            }

            if (rootDeclaration == null || slashDeclaration == null)
                return@launchMessageJob

            val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")
            var context: ApplicationCommandContext? = null

            try {
                val args = SlashCommandArguments(
                    SlashCommandArgumentsSource.SlashCommandArgumentsEventSource(event)
                )

                context = ApplicationCommandContext(
                    pantufa,
                    event
                )


                if (slashDeclaration.requireMinecraftAccount) {
                    val discordAccount = pantufa.getDiscordAccountFromId(event.user.idLong)

                    if (discordAccount == null || !discordAccount.isConnected) {
                        context.reply(false) {
                            styled(
                                "Você precisa associar a sua conta do SparklyPower antes de poder usar este comando! Para associar, use `-registrar NomeNoServidor`!",
                                Constants.ERROR
                            )
                        }
                        return@launchMessageJob
                    } else {
                        context.discordAccount = discordAccount

                        val user = transaction(Databases.sparklyPower) {
                            User.find { Users.id eq context.discordAccount!!.minecraftId }.firstOrNull()
                        }

                        if (user == null) {
                            context.reply(false) {
                                styled(
                                    "Parece que você tem uma conta associada, mas não existe o seu username salvo no banco de dados! Bug?",
                                    Constants.ERROR
                                )
                            }
                            return@launchMessageJob
                        }

                        val userBan = context.getUserBanned(discordAccount.minecraftId)

                        if (userBan != null) {
                            context.reply(false) {
                                styled(
                                    "Você está banido do SparklyPower!"
                                )
                            }
                            return@launchMessageJob
                        }

                        context.sparklyPlayer = user
                    }
                }

                executor.execute(context, args)
            } catch (e: Exception) {
                when (e) {
                    is CommandException -> {
                        context?.reply(e.ephemeral, e.builder)
                    }
                    else -> {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        GlobalScope.launch {
            val componentId = try {
                UnleashedComponentId(event.componentId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            val context: ComponentContext

            try {
                val callbackId = pantufa.interactivityManager.buttonInteractionCallbacks[componentId.uniqueId]

                context = ComponentContext(
                    pantufa,
                    event
                )

                if (callbackId == null) {
                    context.reply(true) {
                        styled(
                            "Opa, parece que os dados de interação desapareceram porque você demorou muito para usá-los! Por favor, use o comando novamente... Desculpe!",
                            "<:pantufa_sleep:1004449850861039646>"
                        )
                    }
                    return@launch
                }

                callbackId.invoke(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        GlobalScope.launch {
            val componentId = try {
                UnleashedComponentId(event.componentId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            val context: ComponentContext?

            try {
                val callback = pantufa.interactivityManager.selectMenuInteractionCallbacks[componentId.uniqueId]

                context = ComponentContext(
                    pantufa,
                    event
                )

                if (callback == null) {
                    context.reply(true) {
                        styled(
                            "Opa, parece que os dados de interação desapareceram porque você demorou muito para usá-los! Por favor, use o comando novamente... Desculpe!",
                            "<:pantufa_sleep:1004449850861039646>"
                        )
                    }
                    return@launch
                }

                callback.invoke(context, event.interaction.values)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        GlobalScope.launch {
            val modalId = try {
                UnleashedComponentId(event.modalId)
            } catch (e: IllegalArgumentException) {
                return@launch
            }

            val context: ModalContext?

            try {
                val modalCallback = pantufa.interactivityManager.modalCallbacks[modalId.uniqueId]
                context = ModalContext(
                    pantufa,
                    event
                )

                if (modalCallback == null) {
                    context.reply(true) {
                        styled(
                            "Opa, parece que os dados de interação desapareceram porque você demorou muito para usá-los! Por favor, use o comando novamente... Desculpe!",
                            "<:pantufa_sleep:1004449850861039646>"
                        )
                    }
                    return@launch
                }

                modalCallback.invoke(context, ModalArguments(event))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        pantufa.launchMessageJob(event) {
            var rootDeclaration: SlashCommandDeclaration? = null
            var slashDeclaration: SlashCommandDeclaration? = null

            for (declaration in manager.slashCommands) {
                val rootLabel = event.name
                val subcommandGroupLabel = event.subcommandGroup
                val subcommandLabel = event.subcommandName

                if (rootLabel == declaration.name) {
                    if (subcommandGroupLabel == null && subcommandLabel == null) {
                        // Already found it, yay!
                        slashDeclaration = declaration
                        rootDeclaration = declaration
                    } else {
                        // Check root subcommands
                        if (subcommandLabel != null) {
                            if (subcommandGroupLabel == null) {
                                // "/name subcommand"
                                slashDeclaration =
                                    declaration.subcommands.firstOrNull { it.name == subcommandLabel }
                                rootDeclaration = declaration
                                break
                            } else {
                                // "/name subcommandGroup subcommand"
                                slashDeclaration = declaration.subcommandGroups.firstOrNull {
                                    it.name == subcommandGroupLabel
                                }
                                    ?.subcommands
                                    ?.firstOrNull {
                                        it.name == subcommandLabel
                                    }
                                rootDeclaration = declaration
                                break
                            }
                        }
                    }
                    break
                }
            }

            if (rootDeclaration == null || slashDeclaration == null)
                return@launchMessageJob

            val executor = slashDeclaration.executor ?: return@launchMessageJob

            val autocompletingOption = executor.options.registeredOptions
                .firstOrNull {
                    it.name == event.focusedOption.name
                } ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the option doesn't exist!")

            try {
                when (autocompletingOption) {
                    is StringDiscordOptionReference ->  {
                        autocompletingOption.choices
                        val autocompleteCallback = autocompletingOption.autocompleteExecutor as? AutocompleteExecutor<String>
                            ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the autocomplete callback doesn't exist!")

                        val map = autocompleteCallback.execute(
                            AutocompleteContext(
                                pantufa,
                                event
                            )
                        ).map {
                            Command.Choice(it.key, it.value)
                        }

                        event.replyChoices(map).await()
                    }

                    is IntDiscordOptionReference -> {
                        val autocompleteCallback = autocompletingOption.autocompleteExecutor as? AutocompleteExecutor<Double>
                            ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the autocomplete callback doesn't exist!")

                        val map = autocompleteCallback.execute(
                            AutocompleteContext(
                                pantufa,
                                event
                            )
                        ).map {
                            Command.Choice(it.key, it.value)
                        }

                        event.replyChoices(map).await()
                    }

                    is LongDiscordOptionReference -> {
                        val autocompleteCallback = autocompletingOption.autocompleteExecutor as? AutocompleteExecutor<Long>
                            ?: error("Received Autocomplete request for ${event.focusedOption.name}, but the autocomplete callback doesn't exist!")

                        val map = autocompleteCallback.execute(
                            AutocompleteContext(
                                pantufa,
                                event
                            )
                        ).map {
                            Command.Choice(it.key, it.value)
                        }

                        event.replyChoices(map).await()
                    }
                    else -> error("Unsupported option reference for autocomplete ${autocompletingOption::class.simpleName}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateCommands(guildId: Long, action: (List<CommandData>) -> (List<Command>)): List<DiscordCommand> {
        logger.info { "Updating slash command on guild $guildId" }

        val applicationCommands = manager.slashCommands.map { manager.convertDeclarationToJDA(it) }

        while (true) {
            try {
                val registeredCommands: List<DiscordCommand>?

                val updatedCommands = action.invoke(applicationCommands)
                val updatedCommandsData = updatedCommands.map {
                    DiscordCommand.from(it)
                }

                registeredCommands = updatedCommandsData

                return registeredCommands
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}