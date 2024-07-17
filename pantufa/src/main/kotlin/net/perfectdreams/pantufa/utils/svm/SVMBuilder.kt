package net.perfectdreams.pantufa.utils.svm

class SVMBuilder(val featureSize: Int, val learningRate: Double = 0.01, val lambda: Double = 0.01) {
    var weights = DoubleArray(featureSize)
    var bias = 0.0

    fun train(data: List<DataPoint>, epochs: Int) {
        repeat(epochs) {
            for (point in data) {
                val prediction = predictRaw(point.features)
                val condition = point.label * prediction

                if (condition < 1) {
                    for (i in weights.indices) {
                        weights[i] = weights[i] + learningRate * (point.label * point.features[i] - 2 * lambda * weights[i])
                    }
                    bias += learningRate * point.label
                } else {
                    for (i in weights.indices) {
                        weights[i] = weights[i] + learningRate * (-2 * lambda * weights[i])
                    }
                }
            }
        }
    }

    private fun predictRaw(features: DoubleArray): Double {
        return features.zip(weights).sumOf { it.first * it.second } + bias
    }
}