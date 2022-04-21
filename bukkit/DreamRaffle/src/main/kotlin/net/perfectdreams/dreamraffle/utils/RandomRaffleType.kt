package net.perfectdreams.dreamraffle.utils

import net.perfectdreams.dreamraffle.raffle.RaffleType
import kotlin.random.Random

val randomRaffleType get() = with (Random.nextDouble()) {
    when {
        this >= .7 -> RaffleType.EXPRESS // 30%
        this >= .45 -> RaffleType.FAST // 25%
        this >= .25 -> RaffleType.TURBO // 20%
        else -> RaffleType.CONVENTIONAL // 25%
    }
}