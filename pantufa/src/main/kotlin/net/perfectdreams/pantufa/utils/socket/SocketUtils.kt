package net.perfectdreams.pantufa.utils.socket

import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonObject
import net.perfectdreams.pantufa.jsonParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ConnectException
import java.net.Socket
import java.util.concurrent.Executors

object SocketUtils {
	val executors = Executors.newCachedThreadPool()

	fun sendAsync(jsonObject: JsonObject, host: String, port: Int, success: ((JsonObject) -> Unit)? = null, error: (() -> Unit)? = null) {
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

		return jsonParser.parse(response).obj
	}
}