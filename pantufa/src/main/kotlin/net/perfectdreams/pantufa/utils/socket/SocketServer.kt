package net.perfectdreams.pantufa.utils.socket

import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import kotlin.concurrent.thread

class SocketServer(val socketPort: Int) {
	var socketHandler: SocketHandler? = null

	fun start() {
		val listener = ServerSocket(socketPort)
		try {
			while (true) {
				val socket = listener.accept()
				try {
					val fromClient = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
					val reply = fromClient.readLine()
					val jsonObject = JsonParser().parse(reply).obj
					val response = JsonObject()
					response["type"] = "noop"

					if (socketHandler != null)
						socketHandler!!.onSocketReceived(jsonObject, response)

					val out = PrintWriter(socket.getOutputStream(), true)
					out.println(response.toString() + "\n")
					out.flush()
				} catch (e: Exception) {
					e.printStackTrace()
				} finally {
					socket.close()
				}
			}
		} finally {
			// println("Shutting down socket server...")
			// listener.close()
		}
	}
}