package net.perfectdreams.dreamclubes.utils

data class KDWrapper(val kills: Long, val deaths: Long) {
    fun getRatio(): Double {
        val deathsOrOne = if (deaths == 0L)
            1
        else deaths

        return kills / deathsOrOne.toDouble()
    }
}