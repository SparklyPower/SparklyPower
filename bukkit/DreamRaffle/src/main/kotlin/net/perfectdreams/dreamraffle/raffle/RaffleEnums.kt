package net.perfectdreams.dreamraffle.raffle

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.time.Duration

enum class RaffleCurrency(val displayName: String, val unitaryPrice: Int) {
    MONEY("sonecas", 250),
    CASH("pesadelos", 5)
}

enum class RaffleType(val displayName: String, val currency: RaffleCurrency, val duration: Duration, val colors: Colors) {
    CONVENTIONAL("convencional", RaffleCurrency.MONEY, 30.minutes, Colors(0x95DE51 to 0xB8F586)),
    EXPRESS("expressa", RaffleCurrency.MONEY, 20.minutes, Colors(0x6CB6EF to 0xB9DCF7)),
    FAST("veloz", RaffleCurrency.MONEY, 15.minutes, Colors(0xF69043 to 0xFAC8A2)),
    TURBO("relâmpago", RaffleCurrency.MONEY, 5.minutes, Colors(0xF07BE4 to 0xF7B5F0)),
    SPECIAL("especial", RaffleCurrency.CASH, 10.minutes, Colors(0xF5CA59 to 0xF9E09D))
}

data class Colors(private val options: Pair<Int, Int>) {
    val default = options.first.asColor
    val light = options.second.asColor

    fun highlight(text: String) = "$light$text$default"
}

private val Int.asColor get() = ChatColor.of(Color(this))
private val Int.minutes get() = Duration.ofMinutes(toLong())