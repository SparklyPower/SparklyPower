package net.perfectdreams.dreamraffle.raffle

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.time.Duration

enum class RaffleCurrency(val displayName: String, val unitaryPrice: Int) {
    SONECAS("sonecas", 250),
    CASH("pesadelos", 25)
}

enum class RaffleType(val currency: RaffleCurrency, val expiresIn: Duration, val colors: Colors) {
    NORMAL(RaffleCurrency.SONECAS, 60.minutes, 0x95DE51.asColor to 0xB8F586.asColor),
    TURBO(RaffleCurrency.SONECAS, 5.minutes, 0xF07BE4.asColor to 0xF7B5F0.asColor),
    CASH(RaffleCurrency.CASH, 15.minutes, 0xF5CA59.asColor to 0xF9E09D.asColor)
}

private typealias Colors = Pair<ChatColor, ChatColor>
private val Int.asColor get() = ChatColor.of(Color(this))
private val Int.minutes get() = Duration.ofMinutes(toLong())