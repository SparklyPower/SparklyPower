package net.perfectdreams.dreamlobbyfun.streamgame.entities

sealed class PlayerAIState(val player: LorittaPlayer) {
    abstract fun tick()

    abstract fun transitionTo(): PlayerAIState?

    open fun enterState() {}

    class IdleState(player: LorittaPlayer) : PlayerAIState(player) {
        val rand = player.m.random.nextInt(2)
        val shynessLevel = player.m.random.nextInt(4)
        val maxElapsed = player.m.random.nextInt(0, 300) // Up to 15s idling
        var elapsedOnGround = 0

        override fun tick() {
            // We will only elapse while Loritta is on ground
            if (player.movementState is PlayerMovementState.IdleState)
                elapsedOnGround++
        }

        // If the current scene has changed since the start of this state, we will trigger a new AI state
        // We will also move if Loritta is colliding with another Loritta
        override fun transitionTo(): PlayerAIState? {
            // If one of the Loritta's is shy, she will try getting away
            val shouldGoAway = shynessLevel == 0 && player.m.entities.filter { it != player }.filterIsInstance<LorittaPlayer>().any { player.m.isCollidingOnIdleState(player, it) }

            return when {
                (elapsedOnGround >= maxElapsed || shouldGoAway) && rand == 0 && player.movementState is PlayerMovementState.IdleState && player.m.isGround(player.x, player.y) != null -> { StrollState(player) }
                (elapsedOnGround >= maxElapsed || shouldGoAway) && rand == 1 && player.movementState is PlayerMovementState.IdleState && player.m.isGround(player.x, player.y) != null -> { JumpingState(player) }
                else -> null
            }
        }
    }

    class JumpingState(player: LorittaPlayer) : PlayerAIState(player) {
        private val rand = player.m.random.nextInt(0, 3)
        private val stayStill = rand == 0
        private val moveToTheRight = rand == 1
        private val moveToTheLeft = rand == 2

        override fun tick() {
            player.jump(player.m.random.nextDouble(20.0, 24.0))

            if (moveToTheRight) {
                player.right()
            } else if (moveToTheLeft) {
                player.left()
            }
        }

        override fun transitionTo() = when {
            player.m.isGround(player.x, player.y) != null -> { IdleState(player) }
            else -> null
        }
    }

    class StrollState(player: LorittaPlayer) : PlayerAIState(player) {
        private val rand = player.m.random.nextInt(0, 5)
        var elapsed = 0
        val maxElapsed = player.m.random.nextInt(20, 100) // Run from 1s to 100s
        var goToRight = (player.m.width / 2) >= player.x
        var jumpIntensity = 8.0

        override fun tick() {
            if (goToRight)
                player.right()
            else
                player.left()

            if (elapsed >= 40) {
                if (rand == 1) {
                    // Random jumps just to be more charming (always tries to jump)
                    player.jump(player.m.random.nextDouble(20.0, 24.0))
                } else if (rand == 2 && player.movementState is PlayerMovementState.RunningState) {
                    // Keep increasing her jump intensity every jump she tries to make
                    player.jump(jumpIntensity)
                    jumpIntensity += 2.0
                } else if (rand == 3) {
                    // Quick jumps in succession (like she is jogging)
                    player.jump(player.m.random.nextDouble(10.0, 12.0))
                } else if (rand == 4) {
                    // Flip her direction
                    goToRight = !goToRight
                }
            }

            elapsed++
        }

        override fun transitionTo() = when {
            elapsed >= maxElapsed -> IdleState(player)
            else -> null
        }
    }
}