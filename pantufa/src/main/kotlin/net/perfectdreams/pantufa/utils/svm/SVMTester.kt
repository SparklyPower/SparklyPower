package net.perfectdreams.pantufa.utils.svm

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object SVMTester {
    @JvmStatic
    fun main(args: Array<String>) {
        var svmSourceFileName = System.getProperty("svmtester.svmFileName")
        if (svmSourceFileName == null) {
            println("Type the trained SVM file name:")
            svmSourceFileName = readln()

            if (!svmSourceFileName.endsWith(".json")) {
                println("Missing .json extension, appending it...")
                svmSourceFileName = "\".json\""
            }
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