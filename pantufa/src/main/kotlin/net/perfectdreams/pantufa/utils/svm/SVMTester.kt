package net.perfectdreams.pantufa.utils.svm

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object SVMTester {
    fun main() {
        var svmSourceFileName = System.getProperty("svmtester.svmFileName")
        if (svmSourceFileName == null) {
            println("Type the trained SVM file name:")
            svmSourceFileName = readln()
        }

        val sourceSVMData = Json.decodeFromString<TrainedSVMData>(
            File(svmSourceFileName)
                .readText()
        )

        interactiveTester(sourceSVMData)
    }

    fun interactiveTester(sourceSVMData: TrainedSVMData) {
        val svm = SparklySVM(SVM(sourceSVMData.weights, sourceSVMData.bias), sourceSVMData.vocabulary)

        println("Successfully loaded SVM!")

        while (true) {
            println("Type!")

            val input = readln()
            val changedInput = normalizeNaiveBayesInput(replaceShortenedWordsWithLongWords(input))

            val result = svm.predictRaw(changedInput)

            println("Result for \"$changedInput\": ${result >= 0} (raw result: ${result})")
        }
    }
}