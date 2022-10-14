package net.perfectdreams.dreamclubes.utils

data class KDWrapper(val kills: Long, val deaths: Long) {
    fun getRatio(): Double {
        return kills / deaths.coerceAtLeast(1).toDouble()
    }
}