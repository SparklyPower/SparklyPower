package net.perfectdreams.dreamlabirinto.events

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.eventmanager.ServerEvent
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamlabirinto.DreamLabirinto
import net.perfectdreams.dreamlabirinto.utils.MazeGenerator
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class EventoLabirinto(val plugin: DreamLabirinto) : ServerEvent("Labirinto", "/labirinto") {
    var wonPlayers = mutableListOf<UUID>()

    init {
        this.delayBetween = 30 * 60 * 1000 // 30m
        this.requiredPlayers = 40 // 40 players
    }

    fun join(player: Player) {
        val spawn = Location(Bukkit.getWorld("Labirinto")!!, 2.0, 80.0, 2.0)
        player.teleport(spawn)
    }

    fun finish(winner: Player) {
        val world = Bukkit.getWorld("Labirinto")!!

        if (wonPlayers.size == 0)
            plugin.config.winner = winner.uniqueId

        // Player venceu a corrida!
        wonPlayers.add(winner.uniqueId)
        val howMuchMoneyWillBeGiven = 15_000 / wonPlayers.size
        val howMuchNightmaresWillBeGiven = 1

        winner.balance += howMuchMoneyWillBeGiven
        scheduler().schedule(plugin, SynchronizationContext.ASYNC) {
            Cash.giveCash(winner, howMuchNightmaresWillBeGiven.toLong())
        }

        winner.fallDistance = 0.0f
        winner.fireTicks = 0
        PlayerUtils.healAndFeed(winner)

        winner.teleportToServerSpawn()

        Bukkit.broadcastMessage("${DreamLabirinto.PREFIX} §b${winner.displayName}§a venceu o labirinto em ${wonPlayers.size}º lugar! Ele ganhou §2$howMuchMoneyWillBeGiven sonhos§a e §c$howMuchNightmaresWillBeGiven pesadelo§a!")

        if (wonPlayers.size == 5) { // Finalizar labirinto
            Bukkit.broadcastMessage("${DreamLabirinto.PREFIX} §eEvento Labirinto acabou, obrigado a todos que participaram! ^-^")

            world.players.forEach {
                it.fallDistance = 0.0f
                it.fireTicks = 0
                PlayerUtils.healAndFeed(it)

                it.teleportToServerSpawn()
            }

            running = false
            lastTime = System.currentTimeMillis()
            wonPlayers.clear()
        }

        return
    }

    override fun preStart() {
        running = true
        start()
    }

    override fun start() {
        generateMaze()

        var idx = 0

        val world = Bukkit.getWorld("Labirinto")!!

        scheduler().schedule(plugin) {
            while (running) {
                if (idx % 3 == 0) {
                    Bukkit.broadcastMessage("${DreamLabirinto.PREFIX} Evento Labirinto começou! §6/labirinto")
                }

                world.players.forEach {
                    it.fallDistance = 0.0f
                    it.fireTicks = 0
                    PlayerUtils.healAndFeed(it)
                    it.activePotionEffects.filter { it.type != PotionEffectType.SPEED && it.type != PotionEffectType.JUMP } .forEach { effect ->
                        it.removePotionEffect(effect.type)
                    }

                    it.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 200, 1, false, false))
                }

                waitFor(100) // 5 segundos
                idx++
            }
        }
    }

    fun generateMaze() {
        with(MazeGenerator(19, 19)) {
            generate(0, 0)

            var currentBlockY = 0
            var currentBlockX = 0

            val world = Bukkit.getWorld("Labirinto")

            val lines = this.displayToLines()

            val endPlate1 = Location(world, 56.0, 79.0, 56.0)

            val randomTextsForSigns = listOf(
                listOf(
                    "",
                    "Loritta é",
                    "§d§lfofa",
                    ""
                ),
                listOf(
                    "",
                    "Pantufa é",
                    "§d§lfofa",
                    ""
                ),
                listOf(
                    "",
                    "Gabriela é",
                    "§d§lfofa",
                    ""
                ),
                listOf(
                    "caminho errado",
                    "parça",
                    "rsrs",
                    ""
                ),
                listOf(
                    "§4§lDia 1:",
                    "Tentando",
                    "encontrar a",
                    "saída..."
                ),
                listOf(
                    "§4§lDia 4:",
                    "Ainda não",
                    "encontrei a",
                    "saída..."
                ),
                listOf(
                    "§4§lDia 8:",
                    "sdds de",
                    "respirar ar",
                    "fresco..."
                ),
                listOf(
                    "§4§lDia 10:",
                    "Passando fome",
                    "Será que o iFood",
                    "entrega aqui?"
                ),
                listOf(
                    "§4§lDia 14:",
                    "Queria sair",
                    "daqui de",
                    "dentro..."
                ),
                listOf(
                    "§4§lDia 20:",
                    "Talvez esteja",
                    "ficando doidin",
                    ""
                ),
                listOf(
                    "§4§lDia 30:",
                    "Saudades daquilo",
                    "que a gente",
                    "não viveu"
                ),
                listOf(
                    "§4§lDia 45:",
                    "...",
                    "não tem saída"
                ),
                listOf(
                    "Será que aqui é",
                    "o caminho certo?",
                    "Hmmmm"
                ),
                listOf(
                    "Acho que aqui é",
                    "o caminho errado"
                ),
                listOf(
                    "Talvez estas",
                    "placas sejam",
                    "apenas lorotas"
                ),
                listOf(
                    "Neymar",
                    "Junior"
                ),
                listOf(
                    "Quarentena",
                    "no Labirinto!!"
                ),
                listOf(
                    "emojo"
                ),
                listOf(
                    "Algum dia você",
                    "irá encontrar",
                    "a saída, né?"
                )
            )

            lines.forEachIndexed { y, line ->
                line.forEachIndexed { x, char ->
                    val blockLocation = Location(world, x.toDouble(), 80.0, currentBlockY.toDouble())

                    val distance = blockLocation.distanceSquared(endPlate1)

                    val type = when {
                        256 >= distance -> Material.GREEN_STAINED_GLASS
                        676 >= distance -> Material.YELLOW_STAINED_GLASS
                        1296 >= distance -> Material.ORANGE_STAINED_GLASS
                        2116 >= distance -> Material.GRAY_STAINED_GLASS
                        else -> Material.BLACK_STAINED_GLASS
                    }

                    Location(world, x.toDouble(), 79.0, currentBlockY.toDouble()).block.type = type
                    Location(world, x.toDouble(), 83.0, currentBlockY.toDouble()).block.type = type

                    repeat(5) {
                        val blockLocation = Location(world, x.toDouble(), 79.0 + it, currentBlockY.toDouble())
                        if (char == '+' || char == '-' || char == '|') {
                            val type = when {
                                256 >= distance -> Material.GREEN_CONCRETE
                                676 >= distance -> Material.YELLOW_CONCRETE
                                1296 >= distance -> Material.ORANGE_CONCRETE
                                2116 >= distance -> Material.GRAY_CONCRETE
                                else -> Material.BLACK_CONCRETE
                            }

                            blockLocation.block.type = type
                        } else {
                            if (it == 2 && chance(1.0)) {
                                val blocc = BlockUtils.attachWallSignAt(blockLocation)

                                if (blocc != null) {
                                    val blockState = blocc.state

                                    if (blockState is Sign) {
                                        randomTextsForSigns.random().forEachIndexed { index, s ->
                                            blockState.setLine(index, s)
                                        }
                                        blockState.update()
                                    }
                                }
                            } else {
                                blockLocation.block.type = if (it == 0 || it == 4) type else Material.AIR
                            }
                        }
                    }
                }
                currentBlockY++
            }

            val startPlates = listOf(
                Location(world, 1.0, 79.0, 1.0),
                Location(world, 2.0, 79.0, 1.0),
                Location(world, 1.0, 79.0, 2.0),
                Location(world, 2.0, 79.0, 2.0)
            )

            val endPlates = listOf(
                Location(world, 56.0, 79.0, 56.0),
                Location(world, 55.0, 79.0, 56.0),
                Location(world, 56.0, 79.0, 55.0),
                Location(world, 55.0, 79.0, 55.0)
            )

            startPlates.forEach {
                it.block.type = Material.DIAMOND_BLOCK
            }

            endPlates.forEach {
                it.block.type = Material.EMERALD_BLOCK
            }
        }
    }
}