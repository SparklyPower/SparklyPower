package net.perfectdreams.pantufa.utils.socket

import com.google.gson.JsonObject

interface SocketHandler {
	fun onSocketReceived(json: JsonObject, response: JsonObject)
}