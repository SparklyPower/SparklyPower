package net.perfectdreams.loritta.morenitta.interactions.commands

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.loritta.morenitta.interactions.InteractionMessage
import net.perfectdreams.loritta.morenitta.interactions.commands.options.*
import net.perfectdreams.loritta.morenitta.utils.extensions.pretty
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Constants

interface CommandContext {
    val jda: JDA
    val pantufa: PantufaBot
    val user: User
    val rootDeclaration: SlashCommandDeclaration
    val commandDeclaration: SlashCommandDeclaration

    suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit): InteractionMessage

    suspend fun explain() {
        val commandDescription = commandDeclaration.description
        val commandPrefix = PantufaBot.PREFIX

        val declarationPath = pantufa.interactionsListener.manager.findDeclarationPath(commandDeclaration)
        val fullLabel = buildString {
            declarationPath.forEach {
                when (it) {
                    is SlashCommandDeclaration -> append(it.name)
                    is SlashCommandGroupDeclaration -> append(it.name)
                }
                append(" ")
            }
        }.trim()

        val commandLabelWithPrefix = "$commandPrefix$fullLabel"

        reply(false) {
            embed {
                author {
                    name = "Clique aqui para ver todos os meus comandos!"
                    url = "https://loritta.website/commands"
                    iconUrl = jda.selfUser.effectiveAvatarUrl
                }

                color = Constants.LORITTA_AQUA.rgb

                title = "<:pantufa_comfy:853048447254396978> `$commandPrefix$fullLabel`"

                description = buildString {
                    append(commandDescription)
                    appendLine()
                    appendLine()
                    append("<:pantufa_smart:997671151587299348> **Como usar**")
                    append(" `")
                    append(commandLabelWithPrefix)
                    append("`")

                    val executor = commandDeclaration.executor

                    if (executor != null) {
                        val options = executor.options.registeredOptions

                        if (options.isNotEmpty()) {
                            append("**")
                            append('`')
                            append(' ')

                            for ((index, option) in options.withIndex()) {
                                if (option is DiscordOptionReference) {
                                    if (option.required)
                                        append("<")
                                    else
                                        append("[")
                                } else append("<")

                                append(option.name)
                                append(":")
                                append(" ")

                                when (option) {
                                    is LongDiscordOptionReference -> append("número inteiro")
                                    is StringDiscordOptionReference -> append("texto")
                                    is IntDiscordOptionReference -> append("número inteiro")
                                    is UserDiscordOptionReference -> append("usuário")
                                    is ChannelDiscordOptionReference -> append("canal")
                                    is AttachmentDiscordOptionReference -> append("arquivo")
                                }

                                if (option is DiscordOptionReference) {
                                    if (option.required)
                                        append(">")
                                    else
                                        append("]")
                                } else append(">")

                                if (index != options.size - 1)
                                    append(' ')
                            }

                            append('`')
                            append("**")
                        }
                    }
                }

                val examplesKey = commandDeclaration.examples
                val examples = ArrayList<String>()

                if (examplesKey.isNotEmpty()) {
                    for (example in examplesKey) {
                        val split = example.split("|-|")
                            .map { it.trim() }

                        if (split.size == 2) {
                            // If the command has a extended description
                            // "12 |-| Gira um dado de 12 lados"
                            // A extended description can also contain "nothing", but contains a extended description
                            // "|-| Gira um dado de 6 lados"
                            val (commandExample, explanation) = split

                            examples.add("\uD83D\uDD39 **$explanation**")
                            examples.add("`" + commandLabelWithPrefix + "`" + (if (commandExample.isEmpty()) "" else "**` $commandExample`**"))
                        } else {
                            val commandExample = split[0]

                            examples.add("`" + commandLabelWithPrefix + "`" + if (commandExample.isEmpty()) "" else "**` $commandExample`**")
                        }
                    }
                }

                if (examples.isNotEmpty()) {
                    field {
                        name = "\uD83D\uDCD6 Exemplos"
                        value = examples.joinToString("\n") { it }
                        inline = false
                    }
                }

                val otherAlternatives = mutableListOf(
                    buildString {
                        append("/")
                        declarationPath.forEach {
                            when (it) {
                                is SlashCommandDeclaration -> append(it.name)
                                is SlashCommandGroupDeclaration -> append(it.name)
                            }
                            append(" ")
                        }
                    }.trim(),
                    buildString {
                        append(commandPrefix)
                        declarationPath.forEach {
                            when (it) {
                                is SlashCommandDeclaration -> append(it.name)
                                is SlashCommandGroupDeclaration -> append(it.name)
                            }
                            append(" ")
                        }
                    }.trim()
                )

                for (alternativeLabel in commandDeclaration.alternativeLegacyLabels) {
                    otherAlternatives.add(
                        buildString {
                            append(commandPrefix)
                            declarationPath.dropLast(1).forEach {
                                when (it) {
                                    is SlashCommandDeclaration -> append(it.name)
                                    is SlashCommandGroupDeclaration -> append(it.name)
                                }
                                append(" ")
                            }
                            append(alternativeLabel)
                        }
                    )
                }

                for (absolutePath in commandDeclaration.alternativeLegacyAbsoluteCommandPaths) {
                    otherAlternatives.add("$commandPrefix$absolutePath")
                }

                if (otherAlternatives.isNotEmpty()) {
                    field {
                        name = "\uD83D\uDD00 Sinônimos"
                        value = otherAlternatives.joinToString(transform = { "`$it`" })
                        inline = false
                    }
                }

                footer {
                    name = "${user.name} • ${commandDeclaration.category.pretty()}"
                    iconUrl = user.effectiveAvatarUrl
                }
            }
        }
    }
}