package net.perfectdreams.dreamblockparty.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.MaterialColors
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import org.bukkit.*
import java.net.URI
import java.util.logging.Level
import javax.imageio.ImageIO

class SkinPlateRenderer(private val blockParty: BlockParty) {
    private val basePlayerSkinPlateX = 48
    private val basePlayerSkinPlateY = 79
    private val basePlayerSkinPlateZ = -1

    lateinit var playerNamesToBeRendered: Channel<String>
    private val playerPlates = mutableListOf<PlayerPlate>()
    private var skinRenderingJob: Job? = null

    fun start(world: World) {
        this.playerNamesToBeRendered = Channel(Channel.UNLIMITED)
        this.skinRenderingJob = blockParty.m.launchAsyncThread {
            var skinPlateX = 0
            var skinPlateY = 0
            var skinPlateZ = 0

            for (playerName in playerNamesToBeRendered) {
                // Already rendered, bail out (may happen if the user leaves the queue and comes back)
                if (playerPlates.any { it.name == playerName })
                    continue

                playerPlates.add(PlayerPlate(playerName))

                try {
                    onMainThread {
                        var x = 0
                        var y = 0
                        repeat(8 * 8) {
                            world.getBlockAt(
                                basePlayerSkinPlateX - (skinPlateX * 8) - x,
                                basePlayerSkinPlateY + skinPlateY,
                                basePlayerSkinPlateZ - (skinPlateZ * 8) - y
                            ).type = Material.WHITE_CONCRETE
                            x++
                            if (x == 8) {
                                x = 0
                                y++
                            }
                        }
                    }

                    val image = ImageIO.read(URI("https://sparklypower.net/api/v1/render/avatar?name=$playerName").toURL())

                    val materialsToBeUsed = mutableListOf<Material>()

                    for (y in 0 until image.height) {
                        for (x in 0 until image.width) {
                            val javaAwtColor = java.awt.Color(image.getRGB(x, y))
                            val material = MaterialColors.getNearestMaterialThatMatchesColor(javaAwtColor)
                            materialsToBeUsed.add(material)
                        }
                    }

                    onMainThread {
                        var x = 0
                        var y = 0
                        for (material in materialsToBeUsed) {
                            world.getBlockAt(
                                basePlayerSkinPlateX - (skinPlateX * 8) - x,
                                basePlayerSkinPlateY + skinPlateY,
                                basePlayerSkinPlateZ - (skinPlateZ * 8) - y
                            ).type = material
                            x++
                            if (x == 8) {
                                x = 0
                                y++
                            }
                        }

                        // Strike lighting after drawing a player's head (on the center of the head)
                        // We do this rand thingy because lighting can only strike a block, so we can never STRIKE directly on the center of the face
                        val randX = DreamUtils.random.nextInt(3, 5)
                        val randZ = DreamUtils.random.nextInt(3, 5)
                        val strikeLocation = Location(
                            world,
                            basePlayerSkinPlateX - (skinPlateX * 8) - randX.toDouble(),
                            (basePlayerSkinPlateY + skinPlateY).toDouble(),
                            basePlayerSkinPlateZ - (skinPlateZ * 8) - randZ.toDouble()
                        )
                        world.strikeLightningEffect(
                            Location(
                                world,
                                basePlayerSkinPlateX - (skinPlateX * 8) - randX.toDouble(),
                                (basePlayerSkinPlateY + skinPlateY).toDouble(),
                                basePlayerSkinPlateZ - (skinPlateZ * 8) - randZ.toDouble()
                            )
                        )

                        world.playSound(
                            strikeLocation,
                            Sound.BLOCK_AMETHYST_BLOCK_HIT,
                            5f,
                            1f
                        )
                    }

                    skinPlateX++
                    if (skinPlateX == 6) {
                        if (skinPlateZ == 5) {
                            if (skinPlateY == 4)
                                return@launchAsyncThread // It will exceed the maximum height, bail out!

                            skinPlateY++
                            skinPlateX = 0
                            skinPlateZ = 0
                        } else {
                            skinPlateZ++
                            skinPlateX = 0
                        }
                    }
                } catch (e: Exception) {
                    blockParty.m.logger.log(Level.WARNING, e) { "Something went wrong while trying to render $playerName's skin on the BlockParty plate!" }
                }
            }
        }
    }

    suspend fun stop() {
        playerPlates.clear()
        skinRenderingJob?.cancelAndJoin()
        playerNamesToBeRendered.close()
    }

    fun addToRenderQueue(playerName: String) = playerNamesToBeRendered.trySend(playerName)

    private data class PlayerPlate(val name: String)
}