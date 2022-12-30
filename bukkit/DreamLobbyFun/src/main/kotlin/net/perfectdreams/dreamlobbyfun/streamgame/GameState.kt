package net.perfectdreams.dreamlobbyfun.streamgame

import io.ktor.util.date.*
import kotlinx.coroutines.delay
import net.perfectdreams.dreamlobbyfun.DreamLobbyFun
import net.perfectdreams.dreamlobbyfun.streamgame.entities.Entity
import net.perfectdreams.dreamlobbyfun.streamgame.entities.LorittaPlayer
import net.perfectdreams.dreamlobbyfun.streamgame.entities.PlayerMovementState
import net.perfectdreams.dreamlobbyfun.streamgame.entities.snapshot.LorittaPlayerSnapshot
import java.util.*
import kotlin.random.Random

class GameState(
    val app: DreamLobbyFun
) {
    companion object {
        const val TICK_DURATION = 50L
    }

    // Inspired by: https://www.twitch.tv/fiotebeardev
    // From PowerChannelPoints
    var snapshot: GameEntitiesSnapshot? = null
    val entities = mutableListOf<Entity>()
    val random = Random(getTimeMillis())
    val width: Int
        get() = app.lobbyImage.width
    val height: Int
        get() = app.lobbyImage.height
    val groundY: Int
        get() = height

    var totalElapsedMS: Long = 0
    var oldTime = getTimeMillis()

    var elapsedTicks = 0

    var solidGround = listOf<Rectangle>(
        FixedRectangle(0, groundY, width, 1000),
    )

    val textures = GameTextures(app)

    fun start() {
        entities.add(
            LorittaPlayer(
                UUID.randomUUID(),
                this,
                100,
                100,
                LorittaPlayer.PlayerType.LORITTA
            )
        )

        entities.add(
            LorittaPlayer(
                UUID.randomUUID(),
                this,
                100,
                100,
                LorittaPlayer.PlayerType.PANTUFA
            )
        )

        entities.add(
            LorittaPlayer(
                UUID.randomUUID(),
                this,
                100,
                100,
                LorittaPlayer.PlayerType.GABRIELA
            )
        )

        // Process the game world on each render
        app.launchAsyncThread {
            while (true) {
                val newTime = getTimeMillis()
                val deltaMS = newTime - oldTime
                oldTime = newTime

                while (totalElapsedMS >= TICK_DURATION) { // game world will be updated every 50ms (20 ticks per second)
                    // println("Running game logic... Total Elapsed MS: $totalElapsedMS")
                    // println("Loritta Location: x: ${(entities.first() as LorittaPlayer).x}; y: ${(entities.first() as LorittaPlayer).y}")

                    // Snapshot current state
                    snapshot = GameEntitiesSnapshot(
                        System.currentTimeMillis(),
                        elapsedTicks,
                        entities.associate { it.id to it.snapshot() }
                    )

                    // Remove dead entities
                    entities.removeAll { it.dead }

                    // Update dynamic rectangles
                    // solidGround.filterIsInstance<ElementBoundingBoxRectangle>().forEach { it.updateCachedCoordinates() }

                    // Tick entities
                    entities.forEach {
                        it.tick()
                    }

                    totalElapsedMS -= TICK_DURATION
                    elapsedTicks++
                }

                // Entity render is not handled here!

                totalElapsedMS += deltaMS

                delay(TICK_DURATION)
            }
        }
    }

    fun isGround(x: Int, y: Int): Rectangle? {
        for (ground in solidGround) {
            if (x in ground.x..(ground.x + ground.width) && y in ground.y..(ground.y + ground.width)) {
                return ground
            }
        }
        return null
    }

    fun isMultiGround(x: Int, y: Int): List<Rectangle> = solidGround.filter { ground ->
        x in ground.x..(ground.x + ground.width) && y in ground.y..(ground.y + ground.width)
    }

    fun isCollidingOnIdleState(player: LorittaPlayer, other: LorittaPlayer) = player.movementState is PlayerMovementState.IdleState && other.movementState is PlayerMovementState.IdleState && player.x in other.x..(other.x + other.width) && player.y in other.y..(other.y + other.width)

    sealed class Rectangle {
        abstract val x: Int
        abstract val y: Int
        abstract val width: Int
        abstract val height: Int
    }

    data class FixedRectangle(
        override val x: Int,
        override val y: Int,
        override val width: Int,
        override val height: Int
    ) : Rectangle()
}