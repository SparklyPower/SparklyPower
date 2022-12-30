package net.perfectdreams.dreamlobbyfun.streamgame.entities

sealed class PlayerMovementState(val player: LorittaPlayer) {
    var newState: PlayerMovementState? = null

    abstract fun tick()

    fun handleInput() {
        while (player.inputs.isNotEmpty()) {
            val input = player.inputs.removeFirst()
            handleInput(input)
        }
    }

    open fun handleInput(input: PlayerInput) {}

    abstract fun transitionTo(): PlayerMovementState?

    open fun enterState() {}

    class JumpingState(player: LorittaPlayer) : PlayerMovementState(player) {
        override fun tick() {
            handleInput()
            player.applyGravity()
            // No air friction, woo
            // player.applyFriction()
            player.applyMovement()

            if (player.x !in 1 until player.m.width) {
                newState = HurtState(player)
                player.speed = player.speed * -1
            }
        }

        override fun handleInput(input: PlayerInput) {
            when (input) {
                is PlayerInput.Right -> {
                    // player.speed += 0.5
                }

                is PlayerInput.Left -> {
                    // player.speed -= 0.5
                }

                else -> {}
            }
        }

        override fun transitionTo() = when {
            player.isGround(player.x, player.y) -> IdleState(player)
            else -> null
        }
    }

    class FallingState(player: LorittaPlayer) : PlayerMovementState(player) {
        override fun tick() {
            handleInput()
            player.applyGravity()
            // No air friction, woo
            // player.applyFriction()
            player.applyMovement()
        }

        override fun transitionTo() = when {
            player.isGround(player.x, player.y) -> IdleState(player)
            else -> null
        }
    }

    class HurtState(player: LorittaPlayer) : PlayerMovementState(player) {
        override fun tick() {
            handleInput()
            player.applyGravity()
            // No air friction, woo
            // player.applyFriction()
            player.applyMovement()
        }

        override fun transitionTo() = when {
            player.isGround(player.x, player.y) -> IdleState(player)
            else -> null
        }
    }

    class IdleState(player: LorittaPlayer) : PlayerMovementState(player) {
        var transitionToJump = false

        override fun tick() {
            handleInput()
            player.applyGravity()
            player.applyFriction()
            player.applyMovement()
        }

        override fun handleInput(input: PlayerInput) {
            when (input) {
                is PlayerInput.Right -> {
                    player.speed += 4.0
                }

                is PlayerInput.Left -> {
                    player.speed -= 4.0
                }

                is PlayerInput.Jump -> {
                    transitionToJump = true
                    player.gravity -= input.power
                }

                else -> {}
            }
        }

        override fun transitionTo() = when {
            transitionToJump -> JumpingState(player)
            player.speed != 0.0 -> RunningState(player)
            !player.isGround(player.x, player.y) -> FallingState(player)
            else -> null
        }
    }

    class RunningState(player: LorittaPlayer) : PlayerMovementState(player) {
        var transitionToJump = false

        override fun tick() {
            handleInput()
            player.applyGravity()
            player.applyFriction()
            player.applyMovement()

            if (player.x !in 1 until player.m.width) {
                newState = HurtState(player)
                player.speed = player.speed * -0.5 // Originally it was -1, but due to the smol screen size it caused the player to keep bouncing back and forth
                player.gravity = -24.0
            }
        }

        override fun handleInput(input: PlayerInput) {
            when (input) {
                is PlayerInput.Right -> {
                    player.speed += 4.0
                }

                is PlayerInput.Left -> {
                    player.speed -= 4.0
                }

                is PlayerInput.Jump -> {
                    transitionToJump = true
                    player.gravity -= input.power
                }
            }
        }

        override fun transitionTo() = when {
            transitionToJump -> JumpingState(player)
            player.speed == 0.0 -> IdleState(player)
            !player.isGround(player.x, player.y) -> FallingState(player)
            else -> null
        }
    }
}