package net.perfectdreams.dreamraffle.raffle

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.random.Random

class RandomnessTest {
    private val ITERATIONS = 100_000
    private val ACCEPTABLE_MARGIN = .1

    @Test
    fun `Assert that randomness is within expected boundaries`() {
        val victories = Array(15) {
            Gambler(UUID.randomUUID(), Random.nextLong(1, 100))
        }.associateWithTo(mutableMapOf()) { 0 }

        val raffle = Raffle(RaffleType.NORMAL).apply {
            victories.keys.forEach { addTickets(it.uuid, it.tickets) }
        }

        repeat (ITERATIONS) {
            val winner = raffle.winner
            with (victories.keys.first { it.uuid == winner.uuid }) {
                victories[this] = victories[this]!! + 1
            }
        }

        victories.forEach {
            val probabilityOfWinning = it.key.tickets.toDouble() / raffle.tickets
            val actualPercentageOfVictories = it.value.toDouble() / ITERATIONS

            assertTrue { actualPercentageOfVictories in with (probabilityOfWinning) {
                this - ACCEPTABLE_MARGIN .. this + ACCEPTABLE_MARGIN
            } }
        }
    }
}