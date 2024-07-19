package net.perfectdreams.pantufa.utils.svm

import kotlinx.serialization.Serializable
import java.text.Normalizer

data class DataPoint(val features: DoubleArray, val label: Int)

fun tokenize(text: String): List<String> {
    return text.lowercase().split("\\s+".toRegex())
}

fun buildVocabulary(texts: List<String>): Map<String, Int> {
    val vocabulary = mutableMapOf<String, Int>()
    var index = 0
    for (text in texts) {
        for (word in tokenize(text)) {
            if (word !in vocabulary) {
                vocabulary[word] = index++
            }
        }
    }
    return vocabulary
}

fun textToFeatures(text: String, vocabulary: Map<String, Int>): DoubleArray {
    val features = DoubleArray(vocabulary.size)
    for (word in tokenize(text)) {
        vocabulary[word]?.let { index ->
            features[index]++
        }
    }
    return features
}

fun prepareData(texts: List<String>, labels: List<Int>): List<DataPoint> {
    if (texts.size != labels.size)
        error("Texts and labels mismatch! ${texts.size} != ${labels.size}")
    val vocabulary = buildVocabulary(texts)
    return texts.zip(labels).map { (text, label) ->
        DataPoint(textToFeatures(text, vocabulary), label)
    }
}


fun replaceShortenedWordsWithLongWords(source: String) = source
    .replace(Regex("\\bSparkly\\b", RegexOption.IGNORE_CASE), "SparklyPower")
    .replace(Regex("\\bservidor\\b", RegexOption.IGNORE_CASE), "SparklyPower")
    .replace(Regex("\\bserver\\b", RegexOption.IGNORE_CASE), "SparklyPower")
    .replace(Regex("\\bspk\\b", RegexOption.IGNORE_CASE), "SparklyPower")
    .replace(Regex("\\bpesa\\b", RegexOption.IGNORE_CASE), "pesadelos")
    .replace(Regex("\\beh\\b", RegexOption.IGNORE_CASE), "é")
    .replace(Regex("\\badissiona\\b", RegexOption.IGNORE_CASE), "adiciona")
    .replace(Regex("\\badissiono\\b", RegexOption.IGNORE_CASE), "adiciono")
    .replace(Regex("\\bcm\\b", RegexOption.IGNORE_CASE), "como")
    .replace(Regex("\\bcnsg\\b", RegexOption.IGNORE_CASE), "consigo")
    .replace(Regex("\\bptg\\b", RegexOption.IGNORE_CASE), "protege")
    .replace(Regex("\\balgm\\b", RegexOption.IGNORE_CASE), "alguém")
    .replace(Regex("\\balg\\b", RegexOption.IGNORE_CASE), "algo")
    .replace(Regex("\\bqm\\b", RegexOption.IGNORE_CASE), "quem")
    .replace(Regex("\\bajd\\b", RegexOption.IGNORE_CASE), "ajuda")
    .replace(Regex("\\btern\\b", RegexOption.IGNORE_CASE), "terreno")
    .replace(Regex("\\bnss\\b", RegexOption.IGNORE_CASE), "nossa")
    .replace(Regex("\\bmds\\b", RegexOption.IGNORE_CASE), "meu deus")
    .replace(Regex("\\bp q\\b", RegexOption.IGNORE_CASE), "porque")
    .replace(Regex("\\bpq\\b", RegexOption.IGNORE_CASE), "porque")
    .replace(Regex("\\bq\\b", RegexOption.IGNORE_CASE), "que")
    .replace(Regex("\\boq\\b", RegexOption.IGNORE_CASE), "o que")
    .replace(Regex("\\bvc\\b", RegexOption.IGNORE_CASE), "você")
    .replace(Regex("\\bvce\\b", RegexOption.IGNORE_CASE), "você")
    .replace(Regex("\\bdc\\b", RegexOption.IGNORE_CASE), "Discord")
    .replace(Regex("\\bdsc\\b", RegexOption.IGNORE_CASE), "Discord")
    .replace(Regex("\\bdisc\\b", RegexOption.IGNORE_CASE), "Discord")
    .replace(Regex("\\bdiscorde\\b", RegexOption.IGNORE_CASE), "qualquer")
    .replace(Regex("\\bql\\b", RegexOption.IGNORE_CASE), "qual")
    .replace(Regex("\\bqlq\\b", RegexOption.IGNORE_CASE), "qualquer")
    .replace(Regex("\\bnn\\b", RegexOption.IGNORE_CASE), "não")

fun normalizeNaiveBayesInput(source: String) = source
    .lowercase()
    .normalize()
    .replace("?", "")
    .replace("!", "")
    .replace(".", "")
    .replace(",", "")
    .replace("\"", "")
    .replace("`", "")
    .replace("<@[0-9]+>".toRegex(), "")
    .trim()

private fun String.normalize(): String {
    val normalizedString = Normalizer.normalize(this, Normalizer.Form.NFD)
    val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    return regex.replace(normalizedString, "")
}

@Serializable
class TrainedSVMData(
    val vocabulary: Map<String, Int>,
    val weights: DoubleArray,
    val bias: Double
)