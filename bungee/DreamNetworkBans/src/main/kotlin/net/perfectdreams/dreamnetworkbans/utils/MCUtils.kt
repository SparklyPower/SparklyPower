package net.perfectdreams.dreamnetworkbans.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import net.perfectdreams.dreamcorebungee.utils.DreamUtils.jsonParser
import java.util.*

/**
 * Classe de utilidades relacionadas ao Minecraft (como UUID query)
 */
object MCUtils {
	private val gson = Gson()

	val username2uuid = mutableMapOf<String, String?>()
	val uuid2profile = mutableMapOf<String, MCTextures?>()

	fun getUniqueId(player: String): String? {
		val lowercase = player.toLowerCase()
		if (username2uuid.contains(lowercase))
			return username2uuid[lowercase]

		if (player.isBlank())
			return null

		val connection = HttpRequest.get("https://api.mojang.com/users/profiles/minecraft/$player")

		if (connection.code() != 200) // 204 == no content, which is used when the account doesn't exist
			return null

		val profile = connection.body()
		val obj = jsonParser.parse(profile)
		username2uuid[obj["name"].string.toLowerCase()] = obj["id"].string

		return username2uuid[lowercase]
	}

	fun getUserProfileFromName(username: String): MCTextures?  {
		val uuid = getUniqueId(username) ?: return null
		return getUserProfile(uuid)
	}

	fun getUserProfile(uuid: String): MCTextures? {
		if (uuid2profile.contains(uuid))
			return uuid2profile[uuid]

		val connection = HttpRequest.get("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
			.contentType("application/json")

		if (connection.code() !in 200..299)
			return null

		val rawJson = connection.body()
		val profile = jsonParser.parse(rawJson).obj

		val textureValue = profile["properties"].array.firstOrNull { it["name"].nullString == "textures" }

		if (textureValue == null) {
			uuid2profile[uuid] = null
			return null
		}

		val str = textureValue["value"].string

		val json = String(Base64.getDecoder().decode(str))

		uuid2profile[uuid] = gson.fromJson(json)
		return uuid2profile[uuid]
	}

	class MCTextures(
		val timestamp: Long,
		val profileId: String,
		val profileName: String,
		val signatureRequired: Boolean?,
		val textures: Map<String, TextureValue>
	)

	class TextureValue(
		val url: String
	)
}