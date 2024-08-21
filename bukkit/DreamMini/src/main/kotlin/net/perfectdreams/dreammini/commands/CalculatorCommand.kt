package net.perfectdreams.dreammini.commands

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.*
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.adventure.*
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreammini.DreamMini
import net.perfectdreams.dreammini.utils.*
import org.bukkit.command.CommandSender

class CalculatorCommand(val m: DreamMini) : SparklyCommand(arrayOf("calc", "calculadora")){
    private fun prefix() = textComponent {
        append("[") {
            color(NamedTextColor.DARK_GRAY)
        }

        append("Calculadora") {
            color(NamedTextColor.GREEN)
            decorate(TextDecoration.BOLD)
        }

        append("]") {
            color(NamedTextColor.DARK_GRAY)
        }
    }

    private fun CommandSender.buildAndSendMessage(block: TextComponent.Builder.() -> (Unit) = {}) = this.sendMessage(
        textComponent(block)
    )

    @Subcommand
    fun root(sender: CommandSender, expression: Array<String>){
        if (expression.isEmpty())
            return sender.sendMessage(
                generateCommandInfo(
                    "calc",
                    mapOf(
                        "Conta" to "Conta a ser calculada."
                    )
                )
            )

        val mathResult = eval(expression.joinToString(" "))
            ?: return sender.buildAndSendMessage {
                color(NamedTextColor.RED)
                append(prefix())
                appendSpace()

                append("Isto não é uma expressão aritmética válida!")
            }

        sender.buildAndSendMessage {
            color(NamedTextColor.GREEN)
            append(prefix())
            appendSpace()

            append("Resultado: $mathResult")
        }
    }

    private fun eval(expression: String): Double? {
        // omg rule of three hiii
        try {
            if (expression.contains("---")) {
                val split = expression.split("/")

                val firstSide = split[0].split("---")
                val secondSide = split[1].split("---")

                val number0 = firstSide[0].trim()
                val number1 = firstSide[1].trim()

                val number2 = secondSide[0].trim()
                val number3 = secondSide[1].trim() // this is the X

                val resultNumber0 = MathUtils.evaluate(number0)
                val resultNumber1 = MathUtils.evaluate(number1)
                val resultNumber2 = MathUtils.evaluate(number2)

                // example
                // resultNumber0 --- resultNumber1
                // resultNumber2 --- x
                return (resultNumber2 * resultNumber1) / resultNumber0
            }

            return MathUtils.evaluate(expression)
        } catch (e: Exception) {
            return null
        }
    }
}
