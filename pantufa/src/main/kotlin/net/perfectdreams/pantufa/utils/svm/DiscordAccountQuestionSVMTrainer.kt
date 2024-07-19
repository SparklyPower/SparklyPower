package net.perfectdreams.pantufa.utils.svm

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    val questionTexts = mutableListOf<String>(
        "Alguém sabe quem é esse aqui no Discord??",
        "Alguém aqui conhece??",
        "Alguém aqui conhece o??",
        "Alguém aqui conhece a??"
    )

    listOf("do", "da", "de").forEach {
        questionTexts.add("Alguém sabe o Discord $it?")
        questionTexts.add("Alguém tem o Discord $it?")
        questionTexts.add("Alguém sabe o Discord $it?")
        questionTexts.add("Alguém sabe qual é o Discord $it?")
        questionTexts.add("Qual o Discord $it?")
        questionTexts.add("Você sabe o Discord $it?")
        questionTexts.add("Olá! Alguém aqui sabe qual o nick $it aqui no Discord?")
        questionTexts.add("Qual é a conta no Discord $it?")
        questionTexts.add("Qual é o @ $it?")
        questionTexts.add("Alguém sabe o @ $it?")
        questionTexts.add("Quem sabe o Discord $it??")
        questionTexts.add("Quem sabe qual é o Discord $it??")
        questionTexts.add("Me passa o Discord $it")
    }

    val trained = SVMTrainer(
        questionTexts,
        File("pantufa/src/main/resources/good-messages.txt")
            .readLines()
            .map { replaceShortenedWordsWithLongWords(it) }
    ).train()

    File("svm-discord-account-question.json")
        .writeText(Json.encodeToString(trained))

    SVMTester.interactiveTester(trained)
}