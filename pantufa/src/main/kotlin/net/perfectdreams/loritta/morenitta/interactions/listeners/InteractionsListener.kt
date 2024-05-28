package net.perfectdreams.loritta.morenitta.interactions.listeners

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclaration
import net.perfectdreams.loritta.morenitta.interactions.commands.UnleashedCommandManager
import net.perfectdreams.pantufa.PantufaBot

class InteractionsListener(val m: PantufaBot) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val manager = m.commandManager
    private var hasAlreadyGloballyUpdatedTheCommands = false

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        GlobalScope.launch {
            var slashDeclaration: SlashCommandDeclaration? = null

            for (declaration in manager.slashCommands) {
                val rootLabel = event.name
                val subcommandGroupLabel = event.subcommandGroup
                val subcommandLabel = event.subcommandName

                if (rootLabel == declaration.name) {
                    if (subcommandGroupLabel == null && subcommandLabel == null) {
                        // Already found it, yay!
                        slashDeclaration = declaration
                    } else {
                        // Check root subcommands
                        if (subcommandLabel != null) {
                            if (subcommandGroupLabel == null) {
                                // "/name subcommand"
                                slashDeclaration =
                                    declaration.subcommands.firstOrNull { it.name == subcommandLabel }
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
                                break
                            }
                        }
                    }
                    break
                }
            }

            // We should throw an error here
            // But we won't because we still use Discord InteraKTions
            if (slashDeclaration == null)
                return@launch

            val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")

            // These variables are used in the catch { ... } block, to make our lives easier
            var context: ApplicationCommandContext? = null

            try {
                val guild = event.guild
                val member = event.member

                val args = SlashCommandArguments(event)
                context = ApplicationCommandContext(event)

                executor.execute(
                    context,
                    args
                )
            } catch (e: Exception) {
                // TODO: Proper catch and throw
                e.printStackTrace()
            }
        }
    }
}