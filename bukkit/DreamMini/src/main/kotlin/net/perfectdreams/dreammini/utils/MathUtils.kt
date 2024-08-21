package net.perfectdreams.dreammini.utils

import kotlin.math.*

object MathUtils {
    private const val DEGREES_TO_RADIANS = 0.017453292519943295

    fun evaluate(input: String) = MathParser(input).parse()

    fun evaluateOrNull(input: String) = try { MathParser(input).parse() } catch (e: RuntimeException) { null }

    fun toRadians(angleDeg: Double): Double {
        return angleDeg * DEGREES_TO_RADIANS
    }

    private class MathParser(val input: String) {
        var pos = -1
        var ch: Int = 0

        fun nextChar() {
            ch = if (++pos < input.length) input[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()

            if (ch == charToEat) {
                nextChar()
                return true
            }

            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < input.length) throw RuntimeException("Unexpected: ${ch.toChar()}")
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()

            while (true) {
                if (eat('+'.code))
                    x += parseTerm() // addition
                else if (eat('-'.code))
                    x -= parseTerm() // subtraction
                else
                    return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()

            while (true) {
                if (eat('*'.code))
                    x *= parseFactor()
                else if (eat('/'.code))
                    x /= parseFactor()
                else if (eat('%'.code))
                    x %= parseFactor()
                else
                    return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos

            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()

                x = input.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()

                val func = input.substring(startPos, pos)
                x = parseFactor()
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "cbrt" -> x.pow(
                        1/3.toDouble()
                    )
                    "sin" -> sin(
                        toRadians(x)
                    )
                    "cos" -> cos(
                        toRadians(x)
                    )
                    "tan" -> tan(
                        toRadians(x)
                    )

                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: ${ch.toChar()}")
            }

            if (eat('^'.code)) x = x.pow(parseFactor())
            if (eat('%'.code)) x %= parseFactor()

            return x
        }
    }
}
