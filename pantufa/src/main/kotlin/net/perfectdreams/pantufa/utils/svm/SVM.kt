package net.perfectdreams.pantufa.utils.svm

class SVM(val weights: DoubleArray, val bias: Double) {
    fun predict(features: DoubleArray): Int {
        return if (predictRaw(features).also { println(it) } >= 0) 1 else -1
    }

    fun predictRaw(features: DoubleArray): Double {
        return features.zip(weights).sumOf { it.first * it.second } + bias
    }
}