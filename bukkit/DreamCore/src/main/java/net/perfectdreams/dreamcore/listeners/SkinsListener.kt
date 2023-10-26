package net.perfectdreams.dreamcore.listeners

import com.destroystokyo.paper.profile.ProfileProperty
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.tables.PlayerSkins
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamcore.utils.skins.AshconEverythingResponse
import net.perfectdreams.dreamcore.utils.skins.StoredDatabaseSkin
import net.perfectdreams.exposedpowerutils.sql.upsert
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
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

            // We only want to auto refresh the skin after 24 hours
            val shouldRefresh = (data == null || (Clock.System.now() - data.configuredAt) >= 24.hours) && data !is StoredDatabaseSkin.CustomMojangSkin

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

                // Set the current player's skin in the database
                transaction(Databases.databaseNetwork) {
                    PlayerSkins.upsert(PlayerSkins.id) {
                        it[PlayerSkins.id] = player.uniqueId
                        it[PlayerSkins.data] = Json.encodeToString<StoredDatabaseSkin>(
                            StoredDatabaseSkin.SelfMojangSkin(
                                ashconResponse.uuid,
                                Clock.System.now(),
                                ashconResponse.textures.raw.value,
                                ashconResponse.textures.raw.signature,
                            )
                        )
                    }
                }

                onMainThread {
                    // And then update our profile with the queried skin!
                    // This works, and it is WAY simpler than the whatever hacky hack SkinsRestorer is doing
                    val playerProfile = player.playerProfile
                    playerProfile.setProperty(
                        ProfileProperty(
                            "textures",
                            ashconResponse.textures.raw.value,
                            ashconResponse.textures.raw.signature,
                        )
                    )
                    player.playerProfile = playerProfile
                }
            }
        }
    }
}