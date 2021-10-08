package net.perfectdreams.dreamcore.commands

import com.mojang.brigadier.Message
import net.perfectdreams.dreamcore.commands.DoYouLikeCommandExecutor.Companion.Options.character
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutorDeclaration
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions

object TestCommand : SparklyCommandDeclarationWrapper {
    override fun declaration() = sparklyCommand(listOf("hello")) {
        subcommand(listOf("world")) {
            executor = HelloWorldCommandExecutor
        }

        subcommand(listOf("lori")) {
            executor = HelloLoriCommandExecutor
        }

        subcommand(listOf("do_you_like")) {
            executor = DoYouLikeCommandExecutor
        }

        subcommand(listOf("tell")) {
            executor = TellExecutor
        }

        executor = HelloCommandExecutor
    }
}

class HelloCommandExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(HelloCommandExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("Só \"Olá\"... para todo mundo... sem definir para quem... uau, que específico você é!")
    }
}

class HelloWorldCommandExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(HelloWorldCommandExecutor::class)

    override fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("Olá mundo!!")
    }
}

class HelloLoriCommandExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(HelloLoriCommandExecutor::class) {
        object Options : CommandOptions() {
            val word = word("word")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("Olá Lori!! fof demais")

        val favoriteWord = args[options.word]

        context.sendMessage("Você disse para a Loritta que a sua palavra favorita é $favoriteWord...")

        val randomThought = listOf(
            "legal",
            "engraçada",
            "divertida",
            "tosca",
            "feia",
            "ridícula",
            "fofa"
        )

        context.sendMessage("Ela achou a palavra ${randomThought.random()}!")
    }
}

class DoYouLikeCommandExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(DoYouLikeCommandExecutor::class) {
        object Options : CommandOptions() {
            val character = word(
                "character",
                suggests = { context, suggestionsBuilder ->
                    suggestionsBuilder.suggest("loritta") { "Loritta Morenitta super fof" }
                    suggestionsBuilder.suggest("pantufa") { "Pantufa!!" }
                    suggestionsBuilder.suggest("gabriela") { "gabriela desenha muito slk" }
                }
            )
                .register()

            val like = boolean("like")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        context.sendMessage("Olá Lori!! fof demais")

        val character = args[options.character]

        val characterName = when (character) {
            "loritta" -> "Loritta Morenitta"
            "pantufa" -> "Pantufa"
            "gabriela" -> "Gabriela"
            else -> throw IllegalArgumentException("I don't know how to handle $character!")
        }

        val result = args[options.like]

        if (result) {
            context.sendMessage("Você gosta da $characterName! ^-^")
        } else {
            context.sendMessage("Você não gosta da $characterName... ;-;")
        }
    }
}

class TellExecutor : SparklyCommandExecutor() {
    companion object : SparklyCommandExecutorDeclaration(TellExecutor::class) {
        object Options : CommandOptions() {
            val player = player("player")
                .register()
        }

        override val options = Options
    }

    override fun execute(context: CommandContext, args: CommandArguments) {
        // TODO: add validation
        // com.mojang.brigadier.exceptions.CommandSyntaxException: No player was found
        val player = args.getAndValidate(options.player)

        context.sendMessage("You selected $player!")
    }
}
