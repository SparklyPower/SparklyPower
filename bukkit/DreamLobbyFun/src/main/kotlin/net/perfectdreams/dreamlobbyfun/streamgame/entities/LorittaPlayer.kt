package net.perfectdreams.dreamlobbyfun.streamgame.entities

import net.perfectdreams.dreamlobbyfun.streamgame.GameState
import net.perfectdreams.dreamlobbyfun.streamgame.entities.snapshot.LorittaPlayerSnapshot
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.math.absoluteValue

class LorittaPlayer(
    id: UUID,
    m: GameState,
    x: Int,
    y: Int,
    val playerType: PlayerType
) : Entity(id, m, x, y) {
    var previousX = x
    var previousY = y
    // The player x, y coordinates are the bottom center of the sprite
    override val width = 128 // sprite.width
    override val height = 128 // sprite.height
    var gravity = 0.0
    var speed = 0.0
    var previousSpeed = 0.0

    var aiState: PlayerAIState = PlayerAIState.IdleState(this)
    var movementState: PlayerMovementState = PlayerMovementState.FallingState(this)

    var inputs = ArrayDeque<PlayerInput>()

    fun jump(power: Double) {
        inputs.add(PlayerInput.Jump(power))
    }

    fun left() {
        inputs.add(PlayerInput.Left)
    }

    fun right() {
        inputs.add(PlayerInput.Right)
    }

    fun applyGravity() {
        // Gravity
        gravity += 1.0
    }

    fun applyFriction() {
        // Friction
        speed *= 0.9

        // println("speed: $speed")

        if (previousSpeed.absoluteValue > speed.absoluteValue && speed.absoluteValue in -0.99..0.99)
            speed = 0.0

        previousSpeed = speed
    }

    fun applyMovement() {
        x += speed.toInt()
        y += gravity.toInt()

        x = x.coerceIn(0, m.width) // Can't leave screen

        val rects = m.isMultiGround(x, y)
        for (rect in rects) {
            if (rect.y >= previousY) {
                y = rect.y
                gravity = 0.0
                break
            }
        }

        previousX = x
        previousY = y
    }

    fun isGround(x: Int, y: Int): Boolean {
        val rects = m.isMultiGround(x, y)
        for (rect in rects) {
            if (rect.y >= previousY) {
                return true
            }
        }

        return false
    }

    override fun tick() {
        // println("X: $x; Y: $y; Speed: $speed; Gravity: $gravity; Current AI state: ${aiState::class}; Movement state: ${movementState::class};")

        aiState.tick()
        val newAIState = aiState.transitionTo()
        if (newAIState != null) {
            newAIState.enterState()
            aiState = newAIState
        }

        // println("Inputs for the movement state: $inputs")

        movementState.tick()
        val newState = movementState.transitionTo() ?: movementState.newState
        if (newState != null) {
            newState.enterState()
            movementState = newState
        }
    }

    override fun remove() {
        dead = true

        val speed = if (speed == 0.0) {
            // If the speed is 0, let's use a random speed
            val rand = m.random.nextBoolean()

            if (rand)
                8.0
            else
                -8.0
        } else {
            // If else, we will invert Loritta's current speed
            -speed
        }

        /* m.renderedObjects.add(
            RenderedDeadPlayer(
                m.textures,
                m.lorittaPetsContainer,
                m.lorittaNametagsContainer,
                x,
                y,
                -32.0,
                speed.coerceIn(-8.0, 8.0),
                owner
            )
        ) */
    }

    override fun snapshot() = LorittaPlayerSnapshot(
        playerType,
        x,
        y,
        speed,
        movementState
    )

    enum class PlayerType {
        LORITTA,
        PANTUFA,
        GABRIELA
    }
}