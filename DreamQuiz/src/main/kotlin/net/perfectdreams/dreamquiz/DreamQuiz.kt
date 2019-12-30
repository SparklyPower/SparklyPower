package net.perfectdreams.dreamquiz

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamquiz.commands.QuizCommand
import net.perfectdreams.dreamquiz.events.Quiz
import net.perfectdreams.dreamquiz.listeners.PlayerListener
import net.perfectdreams.dreamquiz.listeners.TagListener
import net.perfectdreams.dreamquiz.utils.LocationWrapper
import net.perfectdreams.dreamquiz.utils.QuizConfig
import net.perfectdreams.dreamquiz.utils.QuizQuestion
import java.io.File

class DreamQuiz : KotlinPlugin() {

    companion object {
        val PREFIX = "§8[§5§lQuiz§8]§e"

        lateinit var QUIZ: Quiz
        lateinit var CONFIG: QuizConfig
    }

    override fun onEnable() {
        super.onDisable()

        dataFolder.mkdirs()

        val questionsFile = File(dataFolder, "config.json")

        if (!questionsFile.exists()) {
            questionsFile.createNewFile()

            questionsFile.writeText(DreamUtils.gson.toJson(QuizConfig(mutableListOf(), LocationWrapper("world", 0.toDouble(), 0.toDouble(), 0.toDouble(), 0.toFloat(), 0.toFloat()))))
        }

        QUIZ = Quiz(this)
        CONFIG = DreamUtils.gson.fromJson(questionsFile.readText(Charsets.UTF_8), QuizConfig::class.java)

        registerCommand(QuizCommand(this))

        registerEvents(TagListener())
        registerEvents(PlayerListener(this))

        DreamCore.INSTANCE.dreamEventManager.events.add(QUIZ)
    }

    override fun onDisable() {
        super.onDisable()

        DreamCore.INSTANCE.dreamEventManager.events.remove(QUIZ)
    }
}