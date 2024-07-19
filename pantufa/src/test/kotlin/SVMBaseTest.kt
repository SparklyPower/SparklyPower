import kotlinx.serialization.json.Json
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.svm.*
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class SVMBaseTest(
    private val svmName: String,
    private val questionsThatShouldMatch: List<String>,
    private val questionsThatShouldNotMatch: List<String>
) {
    companion object {
        private fun loadSVM(name: String): SparklySVM {
            val trainedSVMData = Json.decodeFromString<TrainedSVMData>(
                PantufaBot::class.java.getResourceAsStream("/support_vector_machines_data/$name.json").readAllBytes().toString(Charsets.UTF_8)
            )

            return SparklySVM(
                SVM(
                    trainedSVMData.weights,
                    trainedSVMData.bias
                ),
                trainedSVMData.vocabulary
            )
        }
    }

    val svm = loadSVM(svmName)

    @Test
    fun testQuestions() {
        for (question in questionsThatShouldMatch) {
            val content = normalizeNaiveBayesInput(question)

            // Content too short, bail out!
            if (2 >= content.split(" ").size)
                error("Question \"$question\" should match, but it is too short!")

            val result = svm.predictWithRawValue(content)
            if (!result.result)
                error("Question \"$question\" should match, but it didn't match! Raw value: ${result.rawValue}")
            else
                println("Question \"$question\" matched! Raw value: ${result.rawValue}")
        }
    }

    @Test
    fun testNotQuestions() {
        for (question in questionsThatShouldNotMatch) {
            val content = normalizeNaiveBayesInput(question)

            val result = svm.predictWithRawValue(content)
            if (result.result)
                error("Question \"$question\" shouldn't, but it did! Raw value: ${result.rawValue}")
            else
                println("Question \"$question\" didn't match! Raw value: ${result.rawValue}")
        }
    }
}