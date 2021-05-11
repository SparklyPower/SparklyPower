package net.perfectdreams.dreamquiz.commands

import com.google.gson.Gson
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.commands.bukkit.SubcommandPermission
import net.perfectdreams.dreamquiz.DreamQuiz
import net.perfectdreams.dreamquiz.utils.QuizConfig
import net.perfectdreams.dreamquiz.utils.QuizQuestion
import net.perfectdreams.dreamquiz.utils.toLocationWrapper
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

class QuizCommand(val m: DreamQuiz) : SparklyCommand(arrayOf("quiz")) {

    @Subcommand
    fun root(sender: Player) {
        if (!DreamQuiz.QUIZ.running)
            return sender.sendMessage("${DreamQuiz.PREFIX} O Evento Quiz não está ocorrendo no momento!")

        if (DreamQuiz.QUIZ.started)
            return sender.sendMessage("${DreamQuiz.PREFIX} O Evento Quiz já começou!")

        val spawn = DreamQuiz.CONFIG.spawn.toLocation()

        sender.sendMessage("${DreamQuiz.PREFIX} Teleportado para o Evento Quiz!")
        sender.teleport(spawn)
    }

    @Subcommand(["force_start"])
    @SubcommandPermission("dreamquiz.admin")
    fun forceStart(sender: CommandSender) {
        if (DreamQuiz.QUIZ.running)
            return sender.sendMessage("${DreamQuiz.PREFIX} O Evento Quiz já está ocorrendo!")

        DreamQuiz.QUIZ.preStart()
    }

    @Subcommand(["set_spawn"])
    @SubcommandPermission("dreamquiz.admin")
    fun setSpawn(sender: Player) {
        DreamQuiz.CONFIG.spawn = sender.location.toLocationWrapper()
        val file = File(m.dataFolder, "config.json")

        file.writeText(DreamUtils.gson.toJson(DreamQuiz.CONFIG))

        sender.sendMessage("${DreamQuiz.PREFIX} Spawn setado com sucesso!")
    }

    @Subcommand(["add_question"])
    @SubcommandPermission("dreamquiz.admin")
    fun addQuestion(sender: CommandSender, answer: String, array: Array<String>) {
        val answer = answer.toBoolean()
        val question = array.joinToString(" ")

        DreamQuiz.CONFIG.questions.add(QuizQuestion(question, answer))
        val file = File(m.dataFolder, "config.json")

        file.writeText(DreamUtils.gson.toJson(DreamQuiz.CONFIG))

        sender.sendMessage("${DreamQuiz.PREFIX} Pergunta adicionada com sucesso!")
    }

    @Subcommand(["reload_config"])
    @SubcommandPermission("dreamquiz.admin")
    fun reloadConfig(sender: CommandSender) {
        val file = File(m.dataFolder, "config.json")

        DreamQuiz.CONFIG = Gson().fromJson(file.readText(), QuizConfig::class.java)

        sender.sendMessage("${DreamQuiz.PREFIX} Config recarregada com sucesso!")
    }
}
