package net.perfectdreams.dreamcore.network.socket

import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.dreamcore.utils.DreamUtils
import org.bukkit.Bukkit
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket

class SocketServer(val socketPort: Int) {
	fun start() {
		val listener = ServerSocket(socketPort, 0, InetAddress.getLoopbackAddress())
		try {
			while (true) {
				val socket = listener.accept()
				GlobalScope.launch {
					try {
						val fromClient = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
						val reply = fromClient.readLine()
						val jsonObject = DreamUtils.jsonParser.parse(reply).obj
						val response = JsonObject()

						val event = SocketReceivedEvent(jsonObject, response)
						Bukkit.getPluginManager().callEvent(event)

						val out = PrintWriter(socket.getOutputStream(), true)
						out.println(event.response.toString() + "\n")
						out.flush()
						fromClient.close()
					} finally {
						socket.close()
					}
				}
			}
		} finally {
			listener.close()
		}
	}
}