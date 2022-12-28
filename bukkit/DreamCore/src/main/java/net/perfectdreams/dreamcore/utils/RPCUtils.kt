package net.perfectdreams.dreamcore.utils

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.DreamCore
import net.sparklypower.rpc.SparklyBungeeRequest
import net.sparklypower.rpc.SparklyBungeeResponse

class RPCUtils(val m: DreamCore) {
    val bungeeCord = BungeeRPCClient()

    class BungeeRPCClient {
        suspend fun send(request: SparklyBungeeRequest): SparklyBungeeResponse {
            val httpResponse = DreamUtils.http.post(DreamCore.dreamConfig.servers.bungeeCord.rpcAddress + "/rpc") {
                userAgent(DreamCore.dreamConfig.serverName)
                setBody(TextContent(Json.encodeToString(request), ContentType.Application.Json))
            }

            return Json.decodeFromString(httpResponse.bodyAsText())
        }
    }
}