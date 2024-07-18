package net.perfectdreams.pantufa.utils.svm

import java.io.File

class SVMTrainer(
    val questionTexts: List<String>,
    val nonQuestionTexts: List<String>
) {
    fun train(): TrainedSVMData {
        // Normalize the questions...
        val questionTexts = questionTexts.map { normalizeNaiveBayesInput(it) }

        // And normalize the non questions too!
        val texts = questionTexts + nonQuestionTexts.map { normalizeNaiveBayesInput(it) }
        val labels = mutableListOf<Int>()
        for (text in questionTexts) {
            labels.add(1)
        }

        for (text in nonQuestionTexts) {
            labels.add(-1)
        }

        val data = prepareData(texts, labels)
        val featureSize = data[0].features.size
        val vocabulary = buildVocabulary(texts)

        val svmBuilder = SVMBuilder(featureSize)
        svmBuilder.train(data, epochs = 1_000)

        return TrainedSVMData(
            vocabulary,
            svmBuilder.weights,
            svmBuilder.bias
        )
    }
}