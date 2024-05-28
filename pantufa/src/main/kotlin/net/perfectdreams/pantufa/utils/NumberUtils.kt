package net.perfectdreams.pantufa.utils

object NumberUtils {
    /**
     * Converts a shortened [String] number (1k, 1.5k, 1M, 2.3kk, etc) to a [Long] number
     *
     * This also converts a normal number (non shortened) to a [Long]
     *
     * @param input the shortened number
     * @return      the number as long or null if it is a non valid (example: text) number
     */
    fun convertShortenedNumberToLong(input: String): Long? {
        val inputAsLowerCase = input.toLowerCase()

        return when {
            inputAsLowerCase.endsWith("m") -> inputAsLowerCase.removeSuffix("m").toDoubleOrNull()?.times(1_000_000)?.toLong()
            inputAsLowerCase.endsWith("kk") -> inputAsLowerCase.removeSuffix("kk").toDoubleOrNull()?.times(1_000_000)?.toLong()
            inputAsLowerCase.endsWith("k") -> inputAsLowerCase.removeSuffix("k").toDoubleOrNull()?.times(1_000)?.toLong()
            else -> inputAsLowerCase.toLongOrNull()
        }
    }
}