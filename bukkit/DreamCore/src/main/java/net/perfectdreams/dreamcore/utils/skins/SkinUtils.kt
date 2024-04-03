package net.perfectdreams.dreamcore.utils.skins

import com.destroystokyo.paper.profile.ProfileProperty
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.tables.PlayerSkins
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.JsonIgnoreUnknownKeys
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class SkinUtils(val m: DreamCore) {
    suspend fun retrieveSkinTexturesBySparklyPowerPlayerName(playerName: String): ProfileProperty? {
        val user = DreamUtils.retrieveUserInfo(playerName) ?: return null
        return retrieveSkinTexturesBySparklyPowerUniqueId(user.id.value)
    }

    suspend fun retrieveSkinTexturesBySparklyPowerUniqueId(uniqueId: UUID): ProfileProperty? {
        val data = transaction(Databases.databaseNetwork) {
            val playerSkin = PlayerSkins.select { PlayerSkins.id eq uniqueId }
                .limit(1)
                .firstOrNull() ?: return@transaction null

            Json.decodeFromString<StoredDatabaseSkin>(playerSkin[PlayerSkins.data])
        } ?: return null

        return if (data is StoredDatabaseSkin.MojangSkin) {
            ProfileProperty(
                "textures",
                data.value,
                data.signature,
            )
        } else null
    }

    suspend fun retrieveSkinTexturesByMojangName(playerName: String): ProfileProperty? {
        val data = retrieveMojangAccountInfo(playerName) ?: return null

        return ProfileProperty(
            "textures",
            data.textures.raw.value,
            data.textures.raw.signature,
        )
    }

    suspend fun retrieveMojangAccountInfo(skinName: String): AshconEverythingResponse? {
        val response = DreamUtils.http.get("https://api.ashcon.app/mojang/v2/user/$skinName")

        if (response.status != HttpStatusCode.OK)
            return null

        return JsonIgnoreUnknownKeys.decodeFromString<AshconEverythingResponse>(response.bodyAsText())
    }

    fun convertNonDashedToUniqueID(id: String): UUID {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32))
    }
}