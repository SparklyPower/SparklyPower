package net.perfectdreams.dreamcore.listeners

import com.destroystokyo.paper.profile.ProfileProperty
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.tables.PlayerSkins
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcore.utils.skins.AshconEverythingResponse
import net.perfectdreams.dreamcore.utils.skins.StoredDatabaseSkin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class SkinsListener(val m: DreamCore) : Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        m.launchAsyncThread {
            m.logger.info { "Retrieving ${player.uniqueId}'s skin from the database..." }
            // Load current skin from the database, if present
            val data = transaction(Databases.databaseNetwork) {
                val playerSkin = PlayerSkins.select { PlayerSkins.id eq e.player.uniqueId }
                    .limit(1)
                    .firstOrNull() ?: return@transaction null

                Json.decodeFromString<StoredDatabaseSkin>(playerSkin[PlayerSkins.data])
            }

            // No skin configured, bail out!
            if (data is StoredDatabaseSkin.NoSkin)
                return@launchAsyncThread

            // We only want to auto refresh the skin after 7 days
            val shouldRefresh = (data == null || (Clock.System.now() - data.configuredAt) >= 7.days) && data !is StoredDatabaseSkin.CustomMojangSkin

            if (!shouldRefresh) {
                if (data is StoredDatabaseSkin.MojangSkin) {
                    onMainThread {
                        // This works, and it is WAY simpler than the whatever hacky hack SkinsRestorer is doing
                        val playerProfile = player.playerProfile
                        playerProfile.setProperty(
                            ProfileProperty(
                                "textures",
                                data.value,
                                data.signature
                            )
                        )
                        player.playerProfile = playerProfile
                    }
                }
            } else {
                // Attempt to get the player's Mojang skin
                val response = DreamUtils.http.get("https://api.ashcon.app/mojang/v2/user/${player.name}")

                if (response.status == HttpStatusCode.NotFound)
                    return@launchAsyncThread

                val ashconResponse = JsonIgnoreUnknownKeys.decodeFromString<AshconEverythingResponse>(response.bodyAsText())

                // We will get the UUID from Ashcon's API, but we will get the skin from Mojang itself
                // We do this because the Username to UUID API is VERY VERY VERY ratelimited
                val mojangResponse = DreamUtils.http.get("https://sessionserver.mojang.com/session/minecraft/profile/${ashconResponse.uuid}?unsigned=false")

                val playerUniqueIdWithDashes: String
                val playerTextureValue: String
                val playerTextureSignature: String

                if (mojangResponse.status == HttpStatusCode.TooManyRequests) {
                    // If we get rate limited, use Ashcon's response
                    playerUniqueIdWithDashes = ashconResponse.uuid
                    playerTextureValue = ashconResponse.textures.raw.value
                    playerTextureSignature = ashconResponse.textures.raw.signature
                } else {
                    // If we aren't rate limited, use Mojang's response
                    val jsonObj = Json.parseToJsonElement(mojangResponse.bodyAsText())
                        .jsonObject

                    val mcTexturesObj = jsonObj["properties"]!!
                        .jsonArray
                        .first {
                            it.jsonObject["name"]!!.jsonPrimitive.content == "textures"
                        }
                        .jsonObject

                    playerUniqueIdWithDashes = m.skinUtils.convertNonDashedToUniqueID(jsonObj["id"]!!.jsonPrimitive.content).toString()
                    playerTextureValue = mcTexturesObj["value"]!!.jsonPrimitive.content
                    playerTextureSignature = mcTexturesObj["signature"]!!.jsonPrimitive.content
                }

                // Set the current player's skin in the database
                transaction(Databases.databaseNetwork) {
                    PlayerSkins.upsert(PlayerSkins.id) {
                        it[PlayerSkins.id] = player.uniqueId
                        it[PlayerSkins.data] = Json.encodeToString<StoredDatabaseSkin>(
                            StoredDatabaseSkin.SelfMojangSkin(
                                playerUniqueIdWithDashes,
                                Clock.System.now(),
                                playerTextureValue,
                                playerTextureSignature
                            )
                        )
                    }
                }

                onMainThread {
                    // Update the player's profile
                    // This works, and it is WAY simpler than the whatever hacky hack SkinsRestorer is doing
                    val playerProfile = player.playerProfile
                    playerProfile.removeProperty("textures")
                    playerProfile.setProperty(
                        ProfileProperty(
                            "textures",
                            playerTextureValue,
                            playerTextureSignature,
                        )
                    )
                    player.playerProfile = playerProfile
                }
            }
        }
    }
}