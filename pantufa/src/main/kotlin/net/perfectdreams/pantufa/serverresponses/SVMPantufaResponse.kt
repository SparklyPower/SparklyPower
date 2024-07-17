package net.perfectdreams.pantufa.serverresponses

import net.perfectdreams.pantufa.serverresponses.sparklypower.HowToResetMyPasswordResponse
import net.perfectdreams.pantufa.utils.svm.SparklySVM
import net.perfectdreams.pantufa.utils.svm.normalizeNaiveBayesInput
import net.perfectdreams.pantufa.utils.svm.replaceShortenedWordsWithLongWords

abstract class SVMPantufaResponse(val svm: SparklySVM) : PantufaResponse {
    override fun handleResponse(message: String): Boolean {
        val unshortenedWordsContent = replaceShortenedWordsWithLongWords(message)
        val content = normalizeNaiveBayesInput(unshortenedWordsContent)
        // Content too short, bail out!
        if (2 >= content.split(" ").size)
            return false

        return svm.predict(content)
    }
}