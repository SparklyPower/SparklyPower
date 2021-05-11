package net.perfectdreams.dreamcore.utils.discord

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.contains
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import net.perfectdreams.dreamcore.utils.DreamUtils
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class DiscordWebhook(val url: String) {
	var requestQueue = mutableListOf<DiscordMessage>()
	var isRateLimited = false
	var cachedThreadPool = Executors.newCachedThreadPool()

	fun send(message: DiscordMessage) {
		if (isRateLimited) {
			requestQueue.add(message)
			return
		}
		cachedThreadPool.submit {
			val response = HttpRequest.post(url)
					.acceptJson()
					.contentType("application/json")
					.header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11") // Why? Because discordapp.com blocks the default User Agent
					.send(DreamUtils.gson.toJson(message))
					.body()

			if (response.isNotEmpty()) { // oh no
				val json = DreamUtils.jsonParser.parse(response).obj

				if (json.contains("retry_after")) { // Rate limited, vamos colocar o request dentro de uma queue
					requestQueue.add(message)
					isRateLimited = true
					thread {
						Thread.sleep(json["retry_after"].long)
						isRateLimited = false

						val requests = requestQueue.toMutableList()
						requestQueue.clear()
						for (queue in requests) {
							send(queue)
						}
					}
				}
			}
		}
	}
}