package net.perfectdreams.pantufa.utils

// Thanks ChatGPT
class NaiveBayes<CATEGORYTYPE> {
    private val classCounts: MutableMap<CATEGORYTYPE, Int> = HashMap()
    val wordCounts: MutableMap<CATEGORYTYPE, MutableMap<String, Int>> = HashMap()
    private var totalDocuments: Int = 0

    fun train(category: CATEGORYTYPE, documents: List<String>) = train(
        documents.map { it to category }
    )

    fun train(documents: List<Pair<String, CATEGORYTYPE>>) {
        for ((text, label) in documents) {
            classCounts[label] = classCounts.getOrDefault(label, 0) + 1
            totalDocuments++

            val words = text.split("\\s+".toRegex()).map { it.toLowerCase() }
            if (!wordCounts.containsKey(label)) {
                wordCounts[label] = HashMap()
            }
            val labelWordCounts = wordCounts[label]!!

            for (word in words) {
                labelWordCounts[word] = labelWordCounts.getOrDefault(word, 0) + 1
            }
        }
    }

    fun classify(text: String) = detailedClassification(text).entries.maxBy { it.value }.key

    fun detailedClassification(text: String): Map<CATEGORYTYPE, Double> {
        val words = text.split("\\s+".toRegex()).map { it.lowercase() }
        val classProbabilities = mutableMapOf<CATEGORYTYPE, Double>()

        for (label in classCounts.keys) {
            val logProbability = Math.log(classCounts[label]!!.toDouble() / totalDocuments)
            var totalWordCountForClass = 0
            wordCounts[label]?.values?.forEach { totalWordCountForClass += it }

            var logProbabilitySum = logProbability
            for (word in words) {
                val wordCount = wordCounts[label]?.getOrDefault(word, 0) ?: 0
                logProbabilitySum += java.lang.Math.log((wordCount + 1).toDouble() / (totalWordCountForClass + wordCounts.size))
            }

            classProbabilities[label] = logProbabilitySum
        }

        // Convert log probabilities to normal probabilities
        val maxLogProbability = classProbabilities.values.maxOrNull() ?: Double.NEGATIVE_INFINITY
        var sumProbabilities = 0.0
        for (label in classProbabilities.keys) {
            val probability = Math.exp(classProbabilities[label]!! - maxLogProbability)
            classProbabilities[label] = probability
            sumProbabilities += probability
        }

        // Normalize the probabilities
        for (label in classProbabilities.keys) {
            classProbabilities[label] = classProbabilities[label]!! / sumProbabilities
        }

        return classProbabilities
    }
}