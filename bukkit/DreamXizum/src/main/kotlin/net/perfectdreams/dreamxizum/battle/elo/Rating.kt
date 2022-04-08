package net.perfectdreams.dreamxizum.battle.elo

import kotlin.math.abs
import kotlin.math.pow

object Rating {
    /**
     * [Ra] is the current rating of the first player. [Rb] is the current rating of the second player.
     * If the first player wins, [Sa] should be 1, if they lose, it should be 0.
     */
    fun calculatePoints(Ra: Int, Rb: Int, Sa: Int): Pair<Int, Int> {
        val Qa = Q(Ra)
        val Qb = Q(Rb)

        val Ea = Qa / (Qa + Qb)
        val Eb = Qb / (Qa + Qb)

        val Sb = abs(Sa - 1)

        return (R(Ra, Ea, Sa) - Ra).toInt() to (R(Rb, Eb, Sb) - Rb).toInt()
    }

    private fun Q(points: Int) = 10.0.pow(points / 400)
    private fun K(points: Int) =
        when {
            points > 2500 -> 32
            points in 1600 .. 2499 -> 48
            else -> 64
        }
    private fun R(points: Int, E: Double, S: Int) = points + K(points) * (S - E)
}