package net.perfectdreams.dreamblockparty.utils

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import com.xxmicloxx.NoteBlockAPI.model.Song
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.perfectdreams.dreamcash.utils.Cash
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.removeAllPotionEffects
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreamblockparty.DreamBlockParty
import net.perfectdreams.dreamcore.utils.adventure.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Vector3f
import java.util.*

class BlockParty(val m: DreamBlockParty) {
    companion object {
        const val WORLD_NAME = "BlockParty"
    }

    val basePlateX = 1
    val basePlateY = 79
    val basePlateZ = -48

    var spawns = mutableListOf<Location>()
    var players = mutableSetOf<Player>()
    var playersInQueue = mutableSetOf<Player>()
    var storedPlayerInventory = mutableMapOf<Player, Array<ItemStack?>>()
    var isStarted = false
    var isPreStart = false
    val isGamePhase
        get() = isStarted && !isPreStart
    val world by lazy { Bukkit.getWorld(WORLD_NAME) }
    val queueSpawn by lazy {
        Location(
            world,
            10.0,
            91.0,
            -54.0,
            0.0f,
            0.0f
        )
    }
    var isServerEvent = false
    var currentEventId = UUID.randomUUID()
    var lastWinner = UUID.randomUUID()
    private val colors = listOf(
        createGameColor(Material.BLACK_TERRACOTTA, "PRETO"),
        createGameColor(Material.RED_TERRACOTTA, "VERMELHO"),
        createGameColor(Material.LIGHT_BLUE_TERRACOTTA, "AZUL"),
        createGameColor(Material.CYAN_TERRACOTTA, "CIANO"),
        createGameColor(Material.PURPLE_TERRACOTTA, "ROXO"),
        createGameColor(Material.MAGENTA_TERRACOTTA, "MAGENTA"),
        createGameColor(Material.PINK_TERRACOTTA, "ROSA"),
        createGameColor(Material.WHITE_TERRACOTTA, "BRANCO"),
        createGameColor(Material.GREEN_TERRACOTTA, "VERDE"),
        createGameColor(Material.YELLOW_TERRACOTTA, "AMARELO"),
        createGameColor(Material.ORANGE_TERRACOTTA, "LARANJA"),
        createGameColor(Material.GRAY_TERRACOTTA, "CINZA"),
    )
    private val colorDisplayTextDisplays = listOf(
        DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(
            m,
            Location(world, 49.0, 86.0, -24.0, 90f, 0f)
        ).addDisplayBlock(),
        DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(
            m,
            Location(world, 25.0, 86.0, 0.0, 180f, 0f)
        ).addDisplayBlock(),
        DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(
            m,
            Location(world, 1.0, 86.0, -23.0, -90f, 0f)
        ).addDisplayBlock(),
        DreamCore.INSTANCE.sparklyDisplayManager.spawnDisplay(
            m,
            Location(world, 24.0, 86.0, -48.0, 0f, 0f)
        ).addDisplayBlock()
    )
    private val songsQueue = mutableListOf<Song>()
    val playersThatProbablyAlreadyLost = mutableListOf<Player>()
    private val skinPlateRenderer = SkinPlateRenderer(this)
    val funnyWinSounds = listOf(
        "perfectdreams.sfx.mizeravi",
        "sparklypower:general.tbh_yippee",
        "sparklypower:general.irra"
    )
    val funnyFailSounds = listOf(
        "perfectdreams.sfx.errou",
        "sparklypower:general.lego_yoda_death",
        "sparklypower:general.xiii"
    )
    val funnyEpicRecoverySounds = listOf(
        "sparklypower:general.oh_my_god",
        "sparklypower:general.xarola_ratinho",
        "sparklypower:general.uepa",
        "sparklypower:general.esse_e_o_meu_patrao_hehe",
        "sparklypower:general.bieltempest_entao_voce_e_o_bonzao",
    )

    private fun refreshSongQueue() {
        songsQueue.clear()
        songsQueue.addAll(m.songs.shuffled())
    }

    fun preStart(isServerEvent: Boolean) {
        this.isServerEvent = isServerEvent
        if (isServerEvent)
            m.eventoBlockParty.running = true

        val eventId = UUID.randomUUID()
        this.currentEventId = eventId
        val world = this.world!!

        spawns.clear()
        spawns.addAll(
            listOf(
                Location(
                    world,
                    24.5,
                    80.0,
                    -23.5,
                    0.0f,
                    0.0f
                )
            )
        )

        playersInQueue.clear()
        isStarted = true
        isPreStart = true

        // Reset plates
        resetGamePlates(world)

        // Reset displays
        colorDisplayTextDisplays.forEach {
            it.text(
                textComponent {
                    color(NamedTextColor.AQUA)
                    decorate(TextDecoration.BOLD)
                    content("BLOCK PARTY")
                }
            )

            it.transformation = Transformation(
                it.transformation.translation,
                it.transformation.leftRotation,
                Vector3f(
                    12f,
                    12f,
                    12f
                ),
                it.transformation.rightRotation
            )

            it.billboard = Display.Billboard.FIXED
            it.isShadowed = true
        }

        skinPlateRenderer.start(world)

        m.launchMainThread {
            val startAt = 60

            for (i in startAt downTo 1) {
                if (currentEventId != eventId) // Parece que o evento acabou e outro começou
                    return@launchMainThread

                val announce = (i in 15..60 && i % 15 == 0) || (i in 0..14 && i % 5 == 0)

                if (announce) {
                    Bukkit.broadcast(
                        textComponent {
                            append(DreamBlockParty.prefix())
                            color(NamedTextColor.YELLOW)
                            appendSpace()
                            append("O Evento Block Party começará em $i segundos! Use ")
                            appendCommand("/blockparty")
                            append(" para entrar!")
                            appendSpace()
                            appendTextComponent {
                                color(NamedTextColor.RED)
                                content("(GUARDE OS ITENS ANTES DE ENTRAR! Vai se dá problema)")
                            }
                        }
                    )
                }

                delayTicks(20)
            }

            if (currentEventId != eventId) // Parece que o evento acabou e outro começou
                return@launchMainThread

            start()
        }
    }

    suspend fun start() {
        isPreStart = false
        val world = this.world!!

        val validPlayersInQueue = playersInQueue.filter { it.isValid && it.world.name == WORLD_NAME }
        playersInQueue.clear()
        playersInQueue.addAll(validPlayersInQueue)
        skinPlateRenderer.stop()

        if (1 >= playersInQueue.size) {
            m.eventoBlockParty.running = false
            m.eventoBlockParty.lastTime = System.currentTimeMillis()
            isStarted = false
            isPreStart = false

            playersInQueue.forEach { player ->
                player.teleportToServerSpawnWithEffects()
                player.sendMessage(
                    textComponent {
                        color(NamedTextColor.RED)
                        append(DreamBlockParty.prefix())
                        appendSpace()
                        append("O Block Party foi cancelado devido a falta de players...")
                    }
                )
            }

            playersInQueue.clear()
            return
        }

        playersInQueue.forEachIndexed { index, player ->
            val locationToTeleportTo = spawns[index % spawns.size]

            PlayerUtils.healAndFeed(player)
            player.removeAllPotionEffects()
            players.add(player)

            val successfullyTeleported = player.teleport(locationToTeleportTo)
            if (!successfullyTeleported) {
                // uuh... that's not good
                // we will skip the finish check because what if it was the first user that caused this issue?
                removeFromGame(player, skipFinishCheck = true)
                return@forEachIndexed
            }
        }

        playersInQueue.forEachIndexed { index, player ->
            val locationToTeleportTo = spawns[index % spawns.size]

            PlayerUtils.healAndFeed(player)
            player.removeAllPotionEffects()
            players.add(player)

            storedPlayerInventory[player] = player.inventory.contents.clone()
            player.openInventory.close()
            player.inventory.clear()

            val successfullyTeleported = player.teleport(locationToTeleportTo)
            if (!successfullyTeleported) {
                // uuh... that's not good
                // we will skip the finish check because what if it was the first user that caused this issue?
                removeFromGame(player, skipFinishCheck = true)
                return@forEachIndexed
            }

            /* player.inventory.addItem(
                ItemStack(Material.IRON_SHOVEL)
                    .rename("§c§lArma de Ovos")
                    .lore("§7Você destruiu o meu ovo!")
            ) */
        }

        // While walking on the player's faces is a bit amusing, it is very disorienting, so let's reset it back
        resetGamePlates(world)

        // Iniciar minigame... daqui a pouquitcho, yay!
        m.launchMainThread {
            // We stay on screen for a bit longer (stay = 30) to avoid flickers when lagging
            players.forEach {
                it.sendTitle("§b§lBLOCK PARTY", "§a5...", 0, 30, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.4f)
            }
            delayTicks(20L)
            players.forEach {
                it.sendTitle("§b§lBLOCK PARTY", "§a4...", 0, 30, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.5f)
            }
            delayTicks(20L)
            players.forEach {
                it.sendTitle("§b§lBLOCK PARTY", "§e3...", 0, 30, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.6f)
            }
            delayTicks(20L)
            players.forEach {
                it.sendTitle("§b§lBLOCK PARTY", "§c2...", 0, 30, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 0.8f)
            }
            delayTicks(20L)
            players.forEach {
                it.sendTitle("§b§lBLOCK PARTY", "§c1...", 0, 30, 0)
                it.playSound(it.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
            }
            delayTicks(20L)
            players.forEach {
                it.sendTitle("§b§lBLOCK PARTY", "§4Sobreviva!", 0, 20, 20)
                it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                it.gameMode = GameMode.ADVENTURE // Avoid issues with users being able to place "lag blocks" to not fall
            }

            var currentRound = 0
            var currentDelayBetweenRounds = 100 // 5 seconds

            while (isStarted) {
                // the plates start at 1 79 -47, they should increase in the xz axis positively
                // each plate is 4x4
                // and the entire arena is 48x48
                // so there are 12 plates * 12 plates = 144 plates
                // plates are organized from
                if (players.size == 1) {
                    finish()
                    continue
                }

                val plates = mutableListOf<GamePlate>()
                repeat(144) {
                    // To make it fair, we will get always the least used color
                    val leastUsedColor = colors.associateWith { plates.count { plate -> it == plate.color } }
                        .entries
                        .shuffled()
                        .minByOrNull { it.value }!!

                    plates.add(GamePlate(leastUsedColor.key))
                }

                // Select a random color for target
                // We will select from the plates because what if the arena doesn't have the selected color? (possible, but very rare)
                val targetColor = plates.random().color

                val targetItem = ItemStack(targetColor.type)
                    .meta<ItemMeta> {
                        displayNameWithoutDecorations {
                            append(targetColor.name)
                        }
                    }

                for (player in players) {
                    repeat(9) {
                        player.inventory.setItem(it, targetItem)
                    }
                }

                // Update display
                colorDisplayTextDisplays.forEach {
                    it.text(targetColor.name)
                }

                // Debugging stuff
                // Bukkit.broadcast(targetColor.name)

                // Fill the plates with the color
                var plateX = 0
                var plateZ = 0
                for (plate in plates) {
                    if (plateX == 12) {
                        plateX = 0
                        plateZ++
                    }

                    // Set blocks in the location to render the plate
                    repeat(4) { blockOffsetZ ->
                        repeat(4) { blockOffsetX ->
                            world.getBlockAt(basePlateX + (plateX * 4) + blockOffsetX, basePlateY, basePlateZ + (plateZ * 4) + blockOffsetZ).type = plate.color.type
                        }
                    }

                    plateX++
                }

                // println("Round $currentRound starting")
                if (songsQueue.isEmpty())
                    refreshSongQueue()

                val songToBePlayed = songsQueue.removeFirst()
                val songPlayer = RadioSongPlayer(songToBePlayed, SoundCategory.RECORDS)

                songPlayer.isPlaying = true
                for (player in players) {
                    player.playSound(
                        player.location,
                        Sound.ENTITY_PLAYER_LEVELUP,
                        1f,
                        1f
                    )
                    songPlayer.addPlayer(player)
                }

                repeat(currentDelayBetweenRounds) {
                    val howManyTicksUntilEnd = currentDelayBetweenRounds - it

                    val shouldTick = howManyTicksUntilEnd != 0 && it != 0 && (howManyTicksUntilEnd % 20 == 0 || howManyTicksUntilEnd == 50 || howManyTicksUntilEnd == 35 || howManyTicksUntilEnd == 30 || howManyTicksUntilEnd == 25 || howManyTicksUntilEnd == 15 || howManyTicksUntilEnd == 10 || howManyTicksUntilEnd == 5 || howManyTicksUntilEnd == 3 || howManyTicksUntilEnd == 2 || howManyTicksUntilEnd == 1)

                    for (player in players) {
                        player.sendExperienceChange(howManyTicksUntilEnd.toFloat() / currentDelayBetweenRounds, currentRound + 1)

                        // We need to manually shift the "tick" sound effect, but how?
                        if (shouldTick) {
                            // println("Round $currentRound - it: $it; howManyTicksUntilEnd: $howManyTicksUntilEnd [TICK!]")
                            // Every second we send a "tick" sfx
                            player.playSound(
                                player.location,
                                Sound.UI_BUTTON_CLICK,
                                1f,
                                when {
                                    howManyTicksUntilEnd == 1 -> 2.5f
                                    howManyTicksUntilEnd == 2 -> 2.2f
                                    howManyTicksUntilEnd == 3 -> 2.0f
                                    howManyTicksUntilEnd == 5 -> 1.8f
                                    howManyTicksUntilEnd == 10 -> 1.6f
                                    howManyTicksUntilEnd == 15 -> 1.4f
                                    // howManyTicksUntilEnd in 20..40 -> 1.2f
                                    else -> 1f
                                }
                            )
                        } else {
                            // println("Round $currentRound - it: $it; howManyTicksUntilEnd: $howManyTicksUntilEnd")
                        }
                    }
                    delayTicks(1L)
                }

                for (player in players) {
                    player.sendExperienceChange(0f, currentRound + 1)
                }

                // Now we do the same thing again, but now to clear the plates!
                plateX = 0
                plateZ = 0
                for (plate in plates) {
                    if (plateX == 12) {
                        plateX = 0
                        plateZ++
                    }

                    // Set blocks in the location to render the plate
                    repeat(4) { blockOffsetZ ->
                        repeat(4) { blockOffsetX ->
                            if (plate.color != targetColor) {
                                world.getBlockAt(
                                    basePlateX + (plateX * 4) + blockOffsetX,
                                    basePlateY,
                                    basePlateZ + (plateZ * 4) + blockOffsetZ
                                ).type = Material.AIR

                                /* world.spawnParticle(
                                    Particle.BLOCK,
                                    (basePlateX + (plateX * 4) + blockOffsetX).toDouble(),
                                    basePlateY.toDouble(),
                                    (basePlateZ + (plateZ * 4) + blockOffsetZ).toDouble(),
                                    4,
                                    plate.color.type.createBlockData()
                                ) */
                            }
                        }
                    }

                    plateX++
                }

                songPlayer.destroy()

                val playersThatProbablyAlreadyLostLocally = mutableListOf<Player>()

                val failSoundToBeUsed = funnyFailSounds.random()

                for (player in players) {
                    player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 10f, 2f)

                    // We also check if the player is on top of the target block or not...
                    var isOnTarget = false
                    for (it in 1..4) {
                        if (player.location.block.getRelative(BlockFace.DOWN, it).type == targetColor.type) {
                            isOnTarget = true
                            break
                        }
                    }

                    // If we aren't on the target, we will just consider that we lost... for now
                    if (!isOnTarget) {
                        // If we are, play a cheeky easter egg hehe
                        player.playSound(player, failSoundToBeUsed, 10f, 1f)
                        playersThatProbablyAlreadyLost.add(player)
                        playersThatProbablyAlreadyLostLocally.add(player)
                    } else {
                        // fake fireworks to not lag the client xd
                        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 10f, 1f)
                        m.launchMainThread {
                            delayTicks(20L)
                            player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 10f, 1f)
                        }
                    }
                }

                colorDisplayTextDisplays.forEach {
                    it.text(
                        textComponent {
                            color(NamedTextColor.GREEN)
                            decorate(TextDecoration.BOLD)
                            content("PARABÉNS!")
                        }
                    )
                }

                delayTicks(20)

                val winSoundToBeUsed = funnyWinSounds.random()
                for (player in players) {
                    if (player !in playersThatProbablyAlreadyLostLocally)
                        player.playSound(player, winSoundToBeUsed, 10.0f, 1.0f)
                }

                delayTicks(40)

                // Rinse and repeat until there is one player left
                currentRound++
                currentDelayBetweenRounds = (currentDelayBetweenRounds - 5).coerceAtLeast(20)
            }
        }
    }

    fun finish() {
        val player = players.firstOrNull()
        if (player == null) {
            isStarted = false
            isPreStart = false
            storedPlayerInventory.clear()

            m.eventoBlockParty.lastTime = System.currentTimeMillis()
            m.eventoBlockParty.running = false

            Bukkit.broadcast(
                textComponent {
                    append(DreamBlockParty.prefix())
                    color(NamedTextColor.RED)
                    appendSpace()
                    append("Parece que o Block Party acabou sem nenhum ganhador... isto é um bug e jamais deveria acontecer!")
                }
            )
            return
        }

        // No need to check if the event has finished for the last player
        removeFromGame(player, skipFinishCheck = true)

        isStarted = false
        isPreStart = false
        storedPlayerInventory.clear()
        playersThatProbablyAlreadyLost.clear()

        m.eventoBlockParty.lastTime = System.currentTimeMillis()
        m.eventoBlockParty.running = false

        val howMuchMoneyWillBeGiven = 50_000
        val howMuchNightmaresWillBeGiven = 1

        Bukkit.broadcast(
            textComponent {
                append(DreamBlockParty.prefix())
                color(NamedTextColor.YELLOW)
                appendSpace()
                appendTextComponent {
                    color(NamedTextColor.AQUA)
                    append(player.displayName())
                }
                append(" venceu o Block Party! Ele ganhou ")
                appendTextComponent {
                    color(NamedTextColor.DARK_GREEN)
                    append("$howMuchMoneyWillBeGiven sonecas")
                }
                append(" e ")
                appendTextComponent {
                    color(NamedTextColor.RED)
                    append("$howMuchNightmaresWillBeGiven pesadelo")
                }
                append("!")
            }
        )

        lastWinner = player.uniqueId
        player.deposit(howMuchMoneyWillBeGiven.toDouble(), TransactionContext(type = TransactionType.EVENTS, extra = "Block Party"))

        /* val map = ItemStack(Material.FILLED_MAP).meta<MapMeta> {
            this.mapId = 26785

            this.displayName(
                Component.text("Venci o evento ")
                    .color(NamedTextColor.YELLOW)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("TNT Run").color(NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD))
                    .append(Component.text("!"))
            )
        }

        DreamMapWatermarker.watermarkMap(map, null)
        player.addItemIfPossibleOrAddToPlayerMailbox(map) */

        scheduler().schedule(m, SynchronizationContext.ASYNC) {
            val wonAt = System.currentTimeMillis()

            DreamCore.INSTANCE.dreamEventManager.addEventVictory(
                player,
                "Block Party",
                wonAt
            )

            Cash.giveCash(player, howMuchNightmaresWillBeGiven.toLong(), TransactionContext(type = TransactionType.EVENTS, extra = "Block Party"))
        }
    }

    fun removeFromGame(player: Player, skipFinishCheck: Boolean) {
        m.logger.info { "Removing ${player.name} from the game. Skip finish check? $skipFinishCheck" }
        if (!players.contains(player))
            return

        // Você perdeu, que sad...
        players.remove(player)
        playersThatProbablyAlreadyLost.remove(player)

        player.world.strikeLightningEffect(player.location)

        // Reset player velocity to avoid them dying before teleporting (due to falling from the tower)
        player.velocity = Vector(0, 0, 0)
        player.teleportToServerSpawnWithEffects()

        // Restaurar o inventário do player
        val storedInventory = storedPlayerInventory[player]
        if (storedInventory != null)
            player.inventory.contents = storedInventory

        player.gameMode = GameMode.SURVIVAL

        if (!skipFinishCheck && isGamePhase && players.size == 1)
            finish()
    }

    fun removeFromQueue(player: Player) {
        m.logger.info { "Removing ${player.name} from the queue" }
        if (!playersInQueue.contains(player))
            return

        player.teleportToServerSpawnWithEffects()
        playersInQueue.remove(player)
    }

    fun joinQueue(player: Player) {
        m.logger.info { "Adding ${player.name} to the queue!" }
        player.teleport(queueSpawn)
        playersInQueue.add(player)
        skinPlateRenderer.addToRenderQueue(player.name)
    }

    private fun resetGamePlates(world: World) {
        // Reset plates
        var plateX = 0
        var plateZ = 0
        for (plate in 0 until 144) {
            if (plateX == 12) {
                plateX = 0
                plateZ++
            }

            // Set blocks in the location to render the plate
            repeat(4) { blockOffsetZ ->
                repeat(4) { blockOffsetX ->
                    world.getBlockAt(
                        basePlateX + (plateX * 4) + blockOffsetX,
                        basePlateY,
                        basePlateZ + (plateZ * 4) + blockOffsetZ
                    ).type = Material.GRAY_CONCRETE

                    // Clear out rendered heads
                    for (y in 1..4) {
                        repeat(4) { blockOffsetY ->
                            world.getBlockAt(
                                basePlateX + (plateX * 4) + blockOffsetX,
                                basePlateY + y,
                                basePlateZ + (plateZ * 4) + blockOffsetZ
                            ).type = Material.AIR
                        }
                    }
                }
            }

            plateX++
        }
    }

    private fun createGameColor(material: Material, name: String): GameColor {
        return GameColor(
            material,
            textComponent {
                // We use "getAverageColorOfMaterial" instead of using the NamedTextColors because the named colors don't *really* match some of the terracotta colors
                color(TextColor.color(MaterialColors.getAverageColorOfMaterial(material)!!.rgb))
                decorate(TextDecoration.BOLD)
                content(name)
            }
        )
    }

    data class GameColor(
        val type: Material,
        val name: Component
    )

    data class GamePlate(
        val color: GameColor
    )
}