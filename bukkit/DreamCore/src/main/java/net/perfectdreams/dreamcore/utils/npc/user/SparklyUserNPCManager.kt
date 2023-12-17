package net.perfectdreams.dreamcore.utils.npc.user

import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.npc.SkinTexture
import java.io.File
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Manages user-created NPCs (not plugin created!)
 */
class SparklyUserNPCManager(val m: DreamCore) {
    val createdNPCs = mutableMapOf<Int, UserCreatedNPC>()
    var configHasBeenLoaded = false

    fun spawn(data: UserCreatedNPCData) {
        val sparklyNPC = m.sparklyNPCManager.spawnFakePlayer(m, data.location.toBukkit(), data.name, skinTextures = data.skin?.textures)
        sparklyNPC.lookClose = data.lookClose

        createdNPCs[data.id] = UserCreatedNPC(
            data,
            sparklyNPC
        )
    }

    fun start() {
        load()

        m.launchAsyncThread {
            while (true) {
                delay(5.minutes)
                save()
            }
        }

        m.launchAsyncThread {
            m.logger.info { "Updating user created NPC skins..." }
            for (npc in createdNPCs) {
                val customSkin = npc.value.data.skin
                if (customSkin != null) {
                    // TODO: Implement skin auto update
                }
            }

            delay(1.hours)
        }
    }

    fun load() {
        m.logger.info { "Loading user created NPCs..." }
        val userNPCsFile = File(m.dataFolder, "user_npcs.json")
        if (userNPCsFile.exists()) {
            val npcDatas = Json.decodeFromString<List<UserCreatedNPCData>>(userNPCsFile.readText())
            npcDatas.forEach {
                spawn(it)
            }
        }
        // Used to avoid saving an empty list if DreamCore for some reason shut down before the SparklyUserNPCManager had a chance to start
        configHasBeenLoaded = true
    }

    fun save() {
        if (configHasBeenLoaded) {
            m.logger.info { "Saving user created NPCs..." }
            File(m.dataFolder, "user_npcs.json")
                .writeText(
                    Json.encodeToString(createdNPCs.map { it.value.data })
                )
        }
    }
}