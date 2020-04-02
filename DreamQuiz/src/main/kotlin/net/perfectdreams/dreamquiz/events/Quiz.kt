package net.perfectdreams.dreamquiz.events

import com.destroystokyo.paper.Title
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.balance
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamquiz.DreamQuiz
import net.perfectdreams.dreamquiz.utils.QuizQuestion
import net.perfectdreams.dreamquiz.utils.prettyBoolean
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class Quiz(val m: DreamQuiz) : ServerEvent("Quiz", "") {
    val alreadyAskedQuestions = mutableListOf<QuizQuestion>()
    val winners = mutableListOf<UUID>()

    val players get() = DreamQuiz.CONFIG.spawn.toLocation().world.players
    var started = false

    override fun getWarmUpAnnouncementMessage(idx: Int): Any {
        return "${DreamQuiz.PREFIX} O Evento Quiz começará em $idx segundos! Use §6/quiz§e para entrar!"
    }

    init {
        this.requiredPlayers = 15
        this.discordAnnouncementRole = "555184377504464898"
        this.delayBetween = 1_800_000 // 30 minutos
        this.command = "/quiz"
    }

    override fun preStart() {
        started = false
        running = true

        countdown()
    }

    override fun start() {
        winners.clear()
        started = true

        players.forEach {
            for (player in Bukkit.getOnlinePlayers()) {
                it.hidePlayer(m, player)
            }
        }

        scheduler().schedule(m) {
            repeat(7) { index ->
                val question = DreamQuiz.CONFIG.questions.filter { it !in alreadyAskedQuestions }.random()
                for (player in players) {
                    player.sendMessage("${DreamQuiz.PREFIX} §2Verdadeiro §eou §4Falso§e? ${question.question}")
                    player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 20 * 15, 0))
                }

                val bar = Bukkit.createBossBar("§6§l${question.question}", BarColor.YELLOW, BarStyle.SEGMENTED_20)
                players.forEach { bar.addPlayer(it) }

                for (i in 10 downTo 1) {
                    players.forEach {
                        it.sendActionBar("§7O resultado será revelado em $i segundo${if (i != 1) "s" else ""}...")
                    }

                    bar.progress -= 0.09
                    waitFor(20)
                }

                players.forEach {
                    bar.removePlayer(it)

                    it.sendMessage("${DreamQuiz.PREFIX} E o resultado é... ${question.answer.prettyBoolean().toUpperCase()}")
                    it.sendTitle(Title("§7E o resultado é...", question.answer.prettyBoolean().toUpperCase(), 0, 60, 0))
                }

                val rightPlayers = getRightPlayers(question.answer)
                val wrongPlayers = getWrongPlayers(question.answer)
                val undefinedPlayers = getUndefinedPlayers()

                for (player in rightPlayers) {
                    val balance = (index + 1) * 268

                    player.sendMessage("${DreamQuiz.PREFIX} Yay! Você acertou! Parabéns! §a+$balance sonhos")

                    player.balance += balance

                    player.playSound(player.location, "perfectdreams.sfx.mizeravi", 10.0f, 1.0f)
                }

                for (player in wrongPlayers) {
                    player.sendMessage("${DreamQuiz.PREFIX} Que pena... você errou! :(")
                    player.teleport(DreamCore.dreamConfig.spawn)

                    for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                        player.showPlayer(m, onlinePlayer)
                    }

                    player.playSound(player.location, "perfectdreams.sfx.errou", 10.0f, 1.0f)
                }

                for (player in undefinedPlayers) {
                    player.sendMessage("${DreamQuiz.PREFIX} Você estava indeciso e não soube escolher nenhum dos dois... Que pena.")
                    player.teleport(DreamCore.dreamConfig.spawn)

                    for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                        player.showPlayer(m, onlinePlayer)
                    }

                    player.playSound(player.location, "perfectdreams.sfx.errou", 10.0f, 1.0f)
                }

                if (players.isEmpty()) {
                    Bukkit.broadcastMessage("${DreamQuiz.PREFIX} O Evento Quiz acabou! Ninguém ganhou o Evento Quiz! Eu achava que vocês eram mais inteligentes...")
                    running = false
                    started = false
                    lastTime = System.currentTimeMillis()

                    return@schedule
                }

                alreadyAskedQuestions.add(question)
                waitFor(3 * 20)

                players.forEach {
                    it.teleport(DreamQuiz.CONFIG.spawn.toLocation())
                }
            }

            running = false
            started = false

            Bukkit.broadcastMessage("${DreamQuiz.PREFIX} O Evento Quiz acabou!")

            val moreThanOne = players.size != 1
            val message = if (players.isNotEmpty()) {
                "${DreamQuiz.PREFIX} Parabéns ao${if (moreThanOne) "s" else ""} player${if (moreThanOne) "s" else ""} ${players.joinToString(", " , transform = { "§b${it.name}§e" })}! Eles ganharam §c1 pesadelo§e!"
            } else {
                "${DreamQuiz.PREFIX} Ninguém ganhou o Evento Quiz! Eu achava que vocês eram mais inteligentes..."
            }

            winners.addAll(players.map { it.uniqueId })

            alreadyAskedQuestions.clear()

            Bukkit.broadcastMessage(message)
    
            lastTime = System.currentTimeMillis()

            switchContext(SynchronizationContext.SYNC)
            val finalPlayersInTheQuiz = players.toMutableList()
            finalPlayersInTheQuiz.forEach {
                it.teleport(DreamCore.dreamConfig.spawn)

                for (player in Bukkit.getOnlinePlayers()) {
                    it.showPlayer(m, player)
                }
            }

            scheduler().schedule(m, SynchronizationContext.ASYNC) {
                finalPlayersInTheQuiz.forEach {
                    Cash.giveCash(it, 1)
                }
            }
        }
    }

    fun getRightPlayers(answer: Boolean): List<Player> {
        return players.filter { it.location.isWithinRegion("$answer") }
    }

    fun getWrongPlayers(answer: Boolean): List<Player> {
        return players.filter { it.location.isWithinRegion("${!answer}") }
    }

    fun getUndefinedPlayers(): List<Player> {
        return players.filter { !it.location.isWithinRegion("true") && !it.location.isWithinRegion("false") }
    }
}
