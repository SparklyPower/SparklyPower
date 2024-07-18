package net.perfectdreams.pantufa.utils.svm

class SparklySVM(private val svm: SVM, private val vocabulary: Map<String, Int>) {
    fun predict(text: String) = svm.predict(textToFeatures(text, vocabulary)) == 1

    fun predictWithRawValue(text: String): PredictionResult {
        val predictedValue = svm.predictRaw(textToFeatures(text, vocabulary))

        return PredictionResult(
            predictedValue >= 0,
            predictedValue
        )
    }

    fun predictRaw(text: String) = svm.predictRaw(textToFeatures(text, vocabulary))

    data class PredictionResult(
        val result: Boolean,
        val rawValue: Double
    )
}