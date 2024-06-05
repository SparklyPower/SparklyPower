package net.perfectdreams.dreamcore.utils.displays.user

import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.minimessage.MiniMessage
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.LocationReference
import net.perfectdreams.dreamcore.utils.displays.DisplayBlock
import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.io.File
import kotlin.time.Duration.Companion.minutes

/**
 * Manages user-created displays (not plugin created!)
 */
class SparklyUserDisplayManager(val m: DreamCore) {
    private val prettyPrintJson = Json {
        prettyPrint = true
    }

    val createdTextDisplays = mutableMapOf<String, UserCreatedSparklyDisplay>()
    var configHasBeenLoaded = false

    fun start() {
        load()

        m.launchAsyncThread {
            while (true) {
                delay(5.minutes)
                save()
            }
        }
    }

    fun load() {
        // Before loading, we need to clean up any created displays (to avoid issues when using the display reload command)
        val createdTextDisplays = this.createdTextDisplays.toMap()
        for (entry in createdTextDisplays) {
            m.sparklyUserDisplayManager.createdTextDisplays.remove(entry.key)
            entry.value.sparklyDisplay.remove()
        }

        m.logger.info { "Loading user created displays..." }
        val userNPCsFile = File(m.dataFolder, "user_displays.json")
        if (userNPCsFile.exists()) {
            val npcDatas = Json.decodeFromString<List<UserCreatedSparklyDisplayData>>(userNPCsFile.readText())
            npcDatas.forEach {
                val sparklyDisplay = m.sparklyDisplayManager.spawnDisplay(m, it.location.toBukkit())
                m.sparklyUserDisplayManager.createdTextDisplays[it.id] = UserCreatedSparklyDisplay(it.id, sparklyDisplay)

                for (block in it.blocks) {
                    when (block) {
                        is UserCreatedSparklyDisplayData.UserCreatedDisplayBlock.UserCreatedItemDropDisplayBlock -> {
                            sparklyDisplay.addItemDropDisplayBlock(ItemStack.deserializeBytes(block.itemStack))
                        }
                        is UserCreatedSparklyDisplayData.UserCreatedDisplayBlock.UserCreatedTextDisplayBlock -> {
                            val newBlock = sparklyDisplay.addDisplayBlock()
                            newBlock.text(block.text?.let { MiniMessage.miniMessage().deserialize(it) })
                            newBlock.billboard = block.billboard
                            newBlock.isShadowed = block.isShadowed
                            newBlock.backgroundColor = Color.fromARGB(block.backgroundColor)
                            newBlock.transformation = Transformation(
                                Vector3f(
                                    block.transformation.translation.x,
                                    block.transformation.translation.y,
                                    block.transformation.translation.z
                                ),
                                Quaternionf(
                                    block.transformation.leftRotation.x,
                                    block.transformation.leftRotation.y,
                                    block.transformation.leftRotation.z,
                                    block.transformation.leftRotation.w,
                                ),
                                Vector3f(
                                    block.transformation.scale.x,
                                    block.transformation.scale.y,
                                    block.transformation.scale.z,
                                ),
                                Quaternionf(
                                    block.transformation.rightRotation.x,
                                    block.transformation.rightRotation.y,
                                    block.transformation.rightRotation.z,
                                    block.transformation.rightRotation.w,
                                ),
                            )
                            // Unused for now
                            // newBlock.lineWidth = block.lineWidth
                        }
                    }
                }

                sparklyDisplay.synchronizeBlocks()
            }
        }
        // Used to avoid saving an empty list if DreamCore for some reason shut down before the SparklyUserNPCManager had a chance to start
        configHasBeenLoaded = true
    }

    fun save() {
        if (configHasBeenLoaded) {
            m.logger.info { "Saving user created displays... ${m}" }
            File(m.dataFolder, "user_displays.json")
                .writeText(
                    prettyPrintJson.encodeToString(
                        createdTextDisplays.map { display ->
                            // println("hologram ${display.key} has ${display.value.sparklyDisplay.blocks.size} blocks")
                            UserCreatedSparklyDisplayData(
                                display.key,
                                LocationReference.fromBukkit(display.value.sparklyDisplay.location),
                                display.value.sparklyDisplay.blocks.map {
                                    when (it) {
                                        is DisplayBlock.TextDisplayBlock -> {
                                            UserCreatedSparklyDisplayData.UserCreatedDisplayBlock.UserCreatedTextDisplayBlock(
                                                it.currentText?.let { MiniMessage.miniMessage().serialize(it) },
                                                it.billboard,
                                                UserCreatedSparklyDisplayData.Transformation(
                                                    UserCreatedSparklyDisplayData.Vector3f(
                                                        it.transformation.translation.x,
                                                        it.transformation.translation.y,
                                                        it.transformation.translation.z
                                                    ),
                                                    UserCreatedSparklyDisplayData.Quaternionf(
                                                        it.transformation.leftRotation.x,
                                                        it.transformation.leftRotation.y,
                                                        it.transformation.leftRotation.z,
                                                        it.transformation.leftRotation.w,
                                                    ),
                                                    UserCreatedSparklyDisplayData.Vector3f(
                                                        it.transformation.scale.x,
                                                        it.transformation.scale.y,
                                                        it.transformation.scale.z,
                                                    ),
                                                    UserCreatedSparklyDisplayData.Quaternionf(
                                                        it.transformation.rightRotation.x,
                                                        it.transformation.rightRotation.y,
                                                        it.transformation.rightRotation.z,
                                                        it.transformation.rightRotation.w,
                                                    ),
                                                ),
                                                it.isShadowed,
                                                it.backgroundColor.asARGB(),
                                                it.lineWidth
                                            )
                                        }

                                        is DisplayBlock.ItemDropDisplayBlock -> UserCreatedSparklyDisplayData.UserCreatedDisplayBlock.UserCreatedItemDropDisplayBlock(
                                            it.itemStack.serializeAsBytes()
                                        )
                                        is DisplayBlock.SpacerDisplayBlock -> TODO()
                                    }
                                }
                            )
                        }
                    )
                )
        }
    }
}