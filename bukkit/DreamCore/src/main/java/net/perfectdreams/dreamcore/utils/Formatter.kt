package net.perfectdreams.dreamcore.utils

import java.math.RoundingMode
import java.text.DecimalFormat

object Formatter {
    private val dividers = mapOf(
        1e12 to "T",
        1e9 to "B",
        1e6 to "M",
        1e3 to "K"
    )

    fun formatMoney(money: Int, upTo: Int = 1): String {
        DecimalFormat("0.${"#".repeat(upTo)}").apply {
            roundingMode = RoundingMode.DOWN
            dividers.forEach {
                if (money >= it.key) return format(
                    money / it.key
                ) + it.value
            }
        }
        return money.toString()
    }
}