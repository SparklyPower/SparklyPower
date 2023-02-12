package net.perfectdreams.dreamcorebungee.network

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcorebungee.DreamCoreBungee
import net.sparklypower.rpc.SparklyBungeeRequest
import net.sparklypower.rpc.SparklyBungeeResponse
import java.io.StringWriter
import java.net.InetSocketAddress
import java.util.*

class APIServer(private val plugin: DreamCoreBungee) {
    private val logger = plugin.logger
    private var server: ApplicationEngine? = null

    fun start() {
        logger.info { "Starting HTTP Server..." }
        val server = embeddedServer(Netty, port = DreamCoreBungee.dreamConfig.rpcPort) {
            routing {
                get("/") {
                    call.respondText("SparklyPower API Web Server")
                }

                post("/rpc") {
                    val jsonPayload = call.receiveText()
                    logger.info { "${call.request.userAgent()} sent a RPC request: $jsonPayload" }
                    val response = when (val request = Json.decodeFromString<SparklyBungeeRequest>(jsonPayload)) {
                        is SparklyBungeeRequest.GetOnlinePlayersRequest -> SparklyBungeeResponse.GetOnlinePlayersResponse(
                            plugin.proxy.serversCopy.entries.associate {
                                it.key to it.value.players.map { player ->
                                    SparklyBungeeResponse.GetOnlinePlayersResponse.ProxyPlayer(
                                        player.name,
                                        player.locale?.toString() ?: "???",
                                        player.ping,
                                        player.isForgeUser
                                    )
                                }
                            }
                        )

                        is SparklyBungeeRequest.TransferPlayersRequest -> {
                            val serverInfo = when (val target = request.transferTarget) {
                                is SparklyBungeeRequest.TransferPlayersRequest.TransferTarget.BungeeServerAddressTarget -> {
                                    plugin.proxy.constructServerInfo(target.name, InetSocketAddress(target.ip, target.port), "", false)
                                }
                                is SparklyBungeeRequest.TransferPlayersRequest.TransferTarget.BungeeServerNameTarget -> {
                                    plugin.proxy.getServerInfo(target.name)
                                }
                            }

                            val players = request.playerIds.map { plugin.proxy.getPlayer(UUID.fromString(it)) }

                            for (player in players) {
                                player.connect(serverInfo)
                            }

                            val playersNotFound = request.playerIds.filter { it !in players.map { it.uniqueId.toString() } }

                            SparklyBungeeResponse.TransferPlayersResponse.Success(playersNotFound)
                        }
                    }

                    call.respondText(
                        Json.encodeToString<SparklyBungeeResponse>(response),
                        ContentType.Application.Json
                    )
                }
            }
        }

        // If set to "wait = true", the server hangs
        this.server = server.start(wait = false)
        logger.info { "Successfully started HTTP Server!" }
    }

    fun stop() {
        val server = server
        if (server != null) {
            logger.info { "Shutting down HTTP Server..." }
            server.stop(0L, 5_000) // 5s for timeout
            logger.info { "Successfully shut down HTTP Server!" }
        } else {
            logger.warning { "HTTP Server wasn't started, so we won't stop it..." }
        }
    }
}