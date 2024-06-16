package net.perfectdreams.loritta.morenitta.interactions.commands

import dev.minn.jda.ktx.interactions.commands.*
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.build.*
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.options.*
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.PantufaReply
import net.perfectdreams.pantufa.api.commands.exceptions.SilentCommandException
import net.perfectdreams.pantufa.api.commands.exceptions.UnleashedCommandException
import net.perfectdreams.pantufa.api.commands.styled
import net.perfectdreams.pantufa.interactions.vanilla.economy.*
import net.perfectdreams.pantufa.interactions.vanilla.discord.*
import net.perfectdreams.pantufa.interactions.vanilla.magic.*
import net.perfectdreams.pantufa.interactions.vanilla.minecraft.*
import net.perfectdreams.pantufa.interactions.vanilla.misc.*
import net.perfectdreams.pantufa.interactions.vanilla.moderation.*
import net.perfectdreams.pantufa.interactions.vanilla.utils.*
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.Users
import net.perfectdreams.pantufa.utils.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.CancellationException
import kotlin.reflect.jvm.jvmName

class UnleashedCommandManager(val m: PantufaBot) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val slashCommands = mutableListOf<SlashCommandDeclaration>()
    private var commandPathToDeclaration = mutableMapOf<String, SlashCommandDeclaration>()

    init {
        logger.info { "Registering Unleashed commands..." }

        // ===[ Discord ]===
        register(PingCommand())

        // ===[ Miscellaneous ]===
        register(GuildsCommand())

        // ===[ Economy ]===
        register(LSXCommand())
        register(PesadelosCommand())
        register(SonecasCommand())
        register(TransactionsCommand())

        // ===[ Magic ]===
        register(ExecuteCommand())

        // ===[ Minecraft ]===
        register(RegisterCommand())
        register(OnlineCommand())
        register(VIPInfoCommand())
        register(ChatColorCommand())
        register(MinecraftCommand())

        // ===[ Moderation ]===
        register(AdminConsoleBungeeCommand())
        register(CommandsLogCommand())
        register(SayCommand())

        // ===[ Utils ]===
        register(ChangePassCommand())
        register(NotificarCommand())
        register(VerificarCommand())
        register(TPSCommand())

        // Transform Slash Commands to Legacy Commands.
        updateCommandPathToDeclarations()
    }

    fun register(declaration: SlashCommandDeclarationWrapper) {
        val builtDeclaration = declaration.command().build()

        if (builtDeclaration.enableLegacyMessageSupport) {
            val executors = mutableListOf<Any>()

            if (builtDeclaration.executor != null)
                executors.add(builtDeclaration.executor)

            for (subcommand in builtDeclaration.subcommands) {
                if (subcommand.executor != null) {
                    executors.add(subcommand.executor)
                }
            }

            for (subcommandGroup in builtDeclaration.subcommandGroups) {
                for (subcommand in subcommandGroup.subcommands) {
                    if (subcommand.executor != null) {
                        executors.add(subcommand.executor)
                    }
                }
            }

            for (executor in executors) {
                if (executor !is LorittaLegacyMessageCommandExecutor) {
                    error("${executor::class.simpleName} does not inherit LorittaLegacyMessageCommandExecutor, but enable legacy message support is enabled!")                }
            }
        }

        logger.info { "Registering command ${declaration::class.jvmName}!" }

        slashCommands += builtDeclaration
    }

    /**
     * Converts a InteraKTions Unleashed [declaration] to JDA
     */
    fun convertDeclarationToJDA(declaration: SlashCommandDeclaration): SlashCommandData {
        return Commands.slash(declaration.name, declaration.description).apply {
            if (declaration.subcommands.isNotEmpty() || declaration.subcommandGroups.isNotEmpty()) {
                if (declaration.executor != null)
                    error("Command ${declaration::class.simpleName} has a root executor, but it also has subcommand/subcommand groups!")

                for (subcommand in declaration.subcommands) {
                    subcommand(subcommand.name, subcommand.description) {
                        val executor = subcommand.executor ?: error("Subcommand does not have a executor!")

                        for (ref in executor.options.registeredOptions) {
                            addOptions(*createOption(ref).toTypedArray())
                        }
                    }
                }

                for (group in declaration.subcommandGroups) {
                    group(group.name, group.description) {
                        for (subcommand in group.subcommands) {
                            subcommand(subcommand.name, subcommand.description) {
                                val executor = subcommand.executor ?: error("Subcommand does not have a executor!")

                                for (ref in executor.options.registeredOptions) {
                                    addOptions(*createOption(ref).toTypedArray())
                                }
                            }
                        }
                    }
                }
            } else {
                val executor = declaration.executor

                if (executor != null) {
                    for (ref in executor.options.registeredOptions) {
                        addOptions(*createOption(ref).toTypedArray())
                    }
                }
            }
        }
    }

    private fun createOption(interaKTionsOption: OptionReference<*>): List<OptionData> {
        when (interaKTionsOption) {
            is DiscordOptionReference -> {
                val description = interaKTionsOption.description

                when (interaKTionsOption) {
                    is LongDiscordOptionReference -> {
                        return listOf(
                            Option<Long>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }

                    is StringDiscordOptionReference -> {
                        return listOf(
                            Option<String>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            ).apply {
                                if (interaKTionsOption.autocompleteExecutor != null) {
                                    isAutoComplete = true
                                }

                                for (choice in interaKTionsOption.choices) {
                                    when (choice) {
                                        is StringDiscordOptionReference.Choice.RawChoice -> choice(choice.name, choice.value)
                                    }
                                }
                            }
                        )
                    }

                    is UserDiscordOptionReference -> {
                        return listOf(
                            Option<User>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }

                    is IntDiscordOptionReference -> {
                        return listOf(
                            Option<Int>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }

                    is ChannelDiscordOptionReference -> {
                        return listOf(
                            Option<GuildChannel>(
                                interaKTionsOption.name,
                                description,
                                interaKTionsOption.required
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun matches(event: MessageReceivedEvent, rawArguments: List<String>): Boolean {
        val start = System.currentTimeMillis()
        var rootDeclaration: SlashCommandDeclaration? = null
        var slashDeclaration: SlashCommandDeclaration? = null

        var argumentsToBeDropped = 0

        var bestMatch: SlashCommandDeclaration? = null
        var absolutePathSize = 0

        commandDeclarationLoop@for((commandPath, declaration) in commandPathToDeclaration) {
            argumentsToBeDropped = 0

            val absolutePathSplit = commandPath.split(" ")

            if (absolutePathSize > absolutePathSplit.size)
                continue

            for ((index, pathSection) in absolutePathSplit.withIndex()) {
                val rawArgument = rawArguments.getOrNull(index)?.lowercase()?.normalize() ?: continue@commandDeclarationLoop

                if (pathSection.normalize() == rawArgument) {
                    argumentsToBeDropped++
                } else {
                    continue@commandDeclarationLoop
                }
            }

            bestMatch = declaration
            absolutePathSize = argumentsToBeDropped
        }

        if (bestMatch != null) {
            rootDeclaration = bestMatch
            slashDeclaration = bestMatch
            argumentsToBeDropped = absolutePathSize
        }

        if (rootDeclaration == null || slashDeclaration == null)
            return false

        val executor = slashDeclaration.executor ?: error("Missing executor on $slashDeclaration!")

        if (executor !is LorittaLegacyMessageCommandExecutor)
            error("$executor doesn't inherit LorittaLegacyMessageCommandExecutor!")

        var context: UnleashedContext? = null

        try {
            val rawArgumentsAfterDrop = rawArguments.drop(argumentsToBeDropped)

            val discordAccount = m.getDiscordAccountFromId(event.author.idLong)

            context = LegacyMessageCommandContext(
                m,
                event,
                rawArgumentsAfterDrop,
                slashDeclaration,
                rootDeclaration
            )

            if (event.message.isFromType(ChannelType.TEXT)) {
                logger.info("(${event.message.guild.name} -> ${event.message.channel.name}) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay}")
            } else {
                logger.info("(Direct Message) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay}")
            }

            if (slashDeclaration.requireMinecraftAccount) {
                if (discordAccount == null || !discordAccount.isConnected) {
                    context.reply(false) {
                        styled(
                            "Você precisa associar a sua conta do SparklyPower antes de poder usar este comando! Para associar, use `-registrar NomeNoServidor`!",
                            Constants.ERROR
                        )
                    }
                    return true
                } else {
                    val user = transaction(Databases.sparklyPower) {
                        net.perfectdreams.pantufa.dao.User.find { Users.id eq discordAccount.minecraftId }.firstOrNull()
                    }

                    if (user == null) {
                        context.reply(false) {
                            styled(
                                "Parece que você tem uma conta associada, mas não existe o seu username salvo no banco de dados! Bug?",
                                Constants.ERROR
                            )
                        }

                        return true
                    }
                }
            }

            val argMap = executor.convertToInteractionsArguments(context, rawArgumentsAfterDrop)

            if (argMap != null) {
                val args = SlashCommandArguments(
                    SlashCommandArgumentsSource.SlashCommandArgumentsMapSource(argMap)
                )

                executor.execute(
                    context,
                    args
                )

                val end = System.currentTimeMillis()

                if (event.message.isFromType(ChannelType.TEXT)) {
                    logger.info("(${event.message.guild.name} -> ${event.message.channel.name}) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay} - OK! Processado em ${end - start}ms")
                } else {
                    logger.info("(Direct Message) ${event.author.name}#${event.author.discriminator} (${event.author.id}): ${event.message.contentDisplay} - OK! Processado em ${end - start}ms")
                }

                return true
            }
        } catch (e: Exception) {
            when (e) {
                is UnleashedCommandException -> {
                    context?.reply(e.ephemeral, e.builder)
                    return true
                }

                is CancellationException -> {
                    logger.error(e) { "RestAction in command ${executor::class.simpleName} has been cancelled" }
                    return true
                }

                is SilentCommandException -> return true
            }

            logger.error { "Exception when executing ${rootDeclaration.name} command! $e" }

            if (!e.message.isNullOrEmpty()) {
                event.channel.sendMessage(
                    PantufaReply(
                        e.message!!,
                        Emotes.PantufaSob.toString()
                    ).build(event.author)
                ).queue()
            }

            return true
        }

        return false
    }

    fun findDeclarationPath(endDeclaration: SlashCommandDeclaration): List<Any> {
        for (declaration in slashCommands) {
            if (declaration == endDeclaration) {
                return listOf(declaration)
            }

            for (subcommandDeclaration in declaration.subcommands) {
                if (subcommandDeclaration == endDeclaration) {
                    return listOf(declaration, subcommandDeclaration)
                }
            }

            for (group in declaration.subcommandGroups) {
                for (subcommandDeclaration in group.subcommands) {
                    if (subcommandDeclaration == endDeclaration) {
                        return listOf(declaration, group, subcommandDeclaration)
                    }
                }
            }
        }

        error("Declaration path is null for $endDeclaration! This should never happen! Are you trying to find a declaration that isn't registered in InteraKTions Unleashed?")
    }

    private fun updateCommandPathToDeclarations() {
        fun isDeclarationExecutable(declaration: SlashCommandDeclaration) = declaration.executor != null

        val commandPathToDeclarations = mutableMapOf<String, SlashCommandDeclaration>()

        fun putNormalized(key: String, value: SlashCommandDeclaration) {
            commandPathToDeclarations[key.normalize()] = value
        }

        for (declaration in slashCommands.filter { it.enableLegacyMessageSupport }) {
            val rootLabels = listOf(declaration.name) + declaration.alternativeLegacyLabels

            if (isDeclarationExecutable(declaration)) {
                for (rootLabel in rootLabels) {
                    putNormalized(rootLabel, declaration)
                }

                for (absolutePath in declaration.alternativeLegacyAbsoluteCommandPaths) {
                    putNormalized(absolutePath, declaration)
                }
            }

            declaration.subcommands.forEach { subcommand ->
                if (isDeclarationExecutable(subcommand)) {
                    val subcommandLabels = listOf(subcommand.name) + subcommand.alternativeLegacyLabels

                    for (rootLabel in rootLabels) {
                        for (subcommandLabel in subcommandLabels) {
                            putNormalized("$rootLabel $subcommandLabel", subcommand)
                        }
                    }

                    for (absolutePath in subcommand.alternativeLegacyAbsoluteCommandPaths) {
                        putNormalized(absolutePath, subcommand)
                    }
                }
            }

            declaration.subcommandGroups.forEach { group ->
                val subcommandGroupLabels = listOf(group.name) + group.alternativeLegacyLabels

                group.subcommands.forEach { subcommand ->
                    if (isDeclarationExecutable(subcommand)) {
                        val subcommandLabels = listOf(subcommand.name) + subcommand.alternativeLegacyLabels

                        for (rootLabel in rootLabels) {
                            for (subcommandGroupLabel in subcommandGroupLabels) {
                                for (subcommandLabel in subcommandLabels) {
                                    putNormalized("$rootLabel $subcommandGroupLabel $subcommandLabel", subcommand)
                                }
                            }
                        }

                        for (absolutePath in subcommand.alternativeLegacyAbsoluteCommandPaths) {
                            putNormalized(absolutePath.normalize(), subcommand)
                        }
                    }
                }
            }
        }
        this.commandPathToDeclaration = commandPathToDeclarations
    }

    private fun String.normalize(): String {
        val original = arrayOf("ę", "š")
        val normalized =  arrayOf("e", "s")

        return this.map { it ->
            val index = original.indexOf(it.toString())
            if (index >= 0) normalized[index] else it
        }.joinToString("")
    }
}