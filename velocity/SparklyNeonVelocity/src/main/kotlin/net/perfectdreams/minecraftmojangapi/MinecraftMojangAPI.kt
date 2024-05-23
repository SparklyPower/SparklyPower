package net.perfectdreams.minecraftmojangapi

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.logger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.minecraftmojangapi.data.MCTextures
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Wrapper for Mojang's Minecraft API, useful for UUID queries and similar
 *
 * https://wiki.vg/Mojang_API
 */
class MinecraftMojangAPI(
    val username2uuid: MutableMap<String, UUID?> = Caffeine.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).maximumSize(10000).build<String, UUID?>().asMap(),
    val uuid2profile: MutableMap<UUID, MCTextures?> = Caffeine.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).maximumSize(10000).build<UUID, MCTextures?>().asMap()
) {
    companion object {
        private val http = HttpClient {
            expectSuccess = false
        }

        private val json = Json {
            ignoreUnknownKeys = true
        }

        private val logger = KotlinLogging.logger {}
    }

    suspend fun getUniqueId(player: String): UUID? {
        val lowercase = player.toLowerCase()
        if (username2uuid.contains(lowercase)) {
            return username2uuid[lowercase]
        }

        if (player.isBlank())
            return null

        val connection = http.get("https://api.mojang.com/users/profiles/minecraft/$player")

        val profile = connection.bodyAsText()
        logger.info { "getUniqueId for $player result is ${connection.status} - $profile" }

        // Mojang uses "404 Not Found" if the profile doesn't exist, or "201 No Content"... yeah, who knows what they actually use
        // Before it was 201, then it was changed to 404, now it is 201 again, smh
        if (connection.status == HttpStatusCode.NotFound || connection.status == HttpStatusCode.NoContent)
            return null

        // Unexpected response, let's throw a exception!
        if (connection.status != HttpStatusCode.OK)
            throw MinecraftMojangAPIException(connection.status)


        val obj = json.parseToJsonElement(profile).jsonObject
        username2uuid[obj["name"]!!.jsonPrimitive.content.toLowerCase()] = convertNonDashedToUniqueID(obj["id"]!!.jsonPrimitive.content)

        return username2uuid[lowercase]
    }

    suspend fun getUserProfileFromName(username: String): MCTextures? {
        val uuid = getUniqueId(username) ?: return null
        return getUserProfile(uuid)
    }

    suspend fun getUserProfile(uuid: UUID): MCTextures? {
        if (uuid2profile.contains(uuid))
            return uuid2profile[uuid]

        val connection = http.get("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
        val rawJson = connection.bodyAsText()
        logger.info { "getUserProfile for $uuid result is ${connection.status} - $rawJson" }

        // Mojang uses "204 No Content" if the profile doesn't exist
        if (connection.status == HttpStatusCode.NoContent)
            return null

        // Unexpected response, let's throw a exception!
        if (connection.status != HttpStatusCode.OK)
            throw MinecraftMojangAPIException(connection.status)

        val profile = json.parseToJsonElement(rawJson).jsonObject

        val textureValue = profile["properties"]!!
            .jsonArray
            .firstOrNull { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull == "textures" }
            ?.jsonObject

        if (textureValue == null) {
            uuid2profile[uuid] = null
            return null
        }

        val str = textureValue["value"]?.jsonPrimitive?.content

        val json = String(Base64.getDecoder().decode(str))

        uuid2profile[uuid] = Companion.json.decodeFromString(json)
        return uuid2profile[uuid]
    }

    private fun convertNonDashedToUniqueID(id: String): UUID {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32))
    }
}