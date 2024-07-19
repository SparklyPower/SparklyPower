package net.perfectdreams.pantufa.utils.svm

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    val dataset = SVMDatasets.howToBuyPesadelos

    val otherDatasets = SVMDatasets.allDatasets.toMutableList()
    otherDatasets.remove(dataset)

    val negativeDataset = File("D:\\SparklyPowerAssets\\SVMQuestionsScratchPad\\base-negative-messages.txt")
        .readLines()
        .toMutableList()
    negativeDataset.addAll(otherDatasets.flatten())

    val trained = SVMTrainer(
        dataset,
        negativeDataset.map { replaceShortenedWordsWithLongWords(it) }
    ).train()

    File("svm-how-to-buy-pesadelos.json")
        .writeText(Json.encodeToString(trained))

    SVMTester.interactiveTester(trained)
}