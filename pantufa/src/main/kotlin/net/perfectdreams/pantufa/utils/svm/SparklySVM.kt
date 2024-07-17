package net.perfectdreams.pantufa.utils.svm

class SparklySVM(private val svm: SVM, private val vocabulary: Map<String, Int>) {
    fun predict(text: String) = svm.predict(textToFeatures(text, vocabulary)) == 1
}