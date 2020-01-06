package net.perfectdreams.dreamcorebungee.network.socket

import com.google.gson.JsonObject
import net.md_5.bungee.api.plugin.Event

class SocketReceivedEvent(val json: JsonObject, var response: JsonObject) : Event()