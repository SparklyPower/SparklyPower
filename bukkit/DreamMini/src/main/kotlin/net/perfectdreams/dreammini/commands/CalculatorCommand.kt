package net.perfectdreams.dreammini.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.generateCommandInfo
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.command.CommandSender

class CalculatorCommand(val m: DreamMini) : SparklyCommand(arrayOf("calc", "calculadora")){

    @Subcommand
    fun root(sender: CommandSender, expression: Array<String>){
        if(expression.isNotEmpty()) {
            try {
                val mathResult = evalMath(expression.joinToString(" "))

                sender.sendMessage("§6§lResultado: §c§l$mathResult")

            } catch (e: Exception) {
                sender.sendMessage("§c§lIsto não é uma expressão aritmética válida!")
            }
        }else{
            sender.sendMessage(generateCommandInfo(
                    "calc",
                    mapOf(
                            "Conta" to "Conta a ser calculada."
                    )
            ))
        }
    }

    private fun evalMath(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Int = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.toInt()))
                        x += parseTerm() // addition
                    else if (eat('-'.toInt()))
                        x -= parseTerm() // subtraction
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.toInt()))
                        x *= parseFactor() // multiplication
                    else if (eat('/'.toInt()))
                        x /= parseFactor() // division
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor() // unary plus
                if (eat('-'.toInt())) return -parseFactor() // unary minus

                var x: Double
                val startPos = this.pos
                if (eat('('.toInt())) { // parentheses
                    x = parseExpression()
                    eat(')'.toInt())
                } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
                    while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
                    while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Unknown function: $func")
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                if (eat('^'.toInt())) x = Math.pow(x, parseFactor()) // exponentiation
                if (eat('%'.toInt())) x %= parseFactor() // mod

                return x
            }
        }.parse()
    }
}