package net.perfectdreams.dreamchat.utils

/**
 * Tries to fit multiple chat tags to form a word
 */
class WordTagFitter(val text: String) {
    val missingWords = text.toCharArray()
    val tagPositions = Array<PlayerTag?>(text.length) { null }

    /**
     * Tries to fit the [tag] into the [text], the tag will be added to the [tagPositions] array
     */
    fun tryFittingInto(tag: PlayerTag) {
        val sizeToBeTaken = tag.small.length

        for (idx in missingWords.indices) {
            val x = missingWords.asList().subList(idx, Math.min(idx + sizeToBeTaken, missingWords.size))

            if (x.map { it.toUpperCase() } == tag.small.toUpperCase().toList()) {
                repeat(x.size) {
                    tagPositions[idx + it] = tag
                }
                break
            }
        }
    }
}