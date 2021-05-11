package net.perfectdreams.dreamcore.network.socket

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import net.perfectdreams.dreamcore.utils.DreamUtils.jsonParser
import net.perfectdreams.dreamcore.utils.SocketCode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.Socket
import java.util.concurrent.Executors

object SocketUtils {
	val executors = Executors.newCachedThreadPool()

	fun sendAsync(jsonObject: JsonObject, host: String = "127.0.0.1", port: Int, success: ((JsonObject) -> Unit)? = null, error: (() -> Unit)? = null) {
		executors.submit {
			try {
				val response = send(jsonObject, host, port)
				success?.invoke(response)
			} catch (e: ConnectException) {
				error?.invoke()
			}
		}
	}

	fun send(jsonObject: JsonObject, host: String = "127.0.0.1", port: Int): JsonObject {
		val s = Socket(host, port)
		val toServer = OutputStreamWriter(s.getOutputStream(), "UTF-8")
		val fromServer = BufferedReader(InputStreamReader(s.getInputStream(), "UTF-8"))

		toServer.write(jsonObject.toString() + "\n")
		toServer.flush()

		val response = fromServer.readLine()
		s.close()
		fromServer.close()

		return jsonParser.parse(response).obj
	}

	/**
	 * Creates an JSON object wrapping the error object
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object containing the error
	 */
	fun createErrorPayload(code: SocketCode, message: String? = null): JsonObject {
		return jsonObject("error" to createErrorObject(code, message))
	}

	/**
	 * Creates an JSON object containing the code error
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object with the error
	 */
	fun createErrorObject(code: SocketCode, message: String? = null): JsonObject {
		val jsonObject = jsonObject(
				"code" to code.errorId,
				"reason" to code.fancyName,
				"help" to "https://perfectdreams.net/"
		)

		if (message != null) {
			jsonObject["message"] = message
		}

		return jsonObject
	}
}