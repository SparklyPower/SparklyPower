package net.sparklypower.sparklyneonvelocity.network

import com.velocitypowered.api.proxy.ProxyServer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.sparklypower.rpc.SparklyBungeeRequest
import net.sparklypower.rpc.SparklyBungeeResponse
import net.sparklypower.sparklyneonvelocity.SparklyNeonVelocity
import java.util.*
import kotlin.jvm.optionals.getOrNull

class APIServer(private val plugin: SparklyNeonVelocity, private val velocityServer: ProxyServer) {
    private val logger = plugin.logger
    private var server: ApplicationEngine? = null

    fun start() {
        logger.info { "Starting HTTP Server..." }
        val server = embeddedServer(Netty, port = plugin.config.rpcPort) {
            routing {
                get("/") {
                    call.respondText("SparklyPower API Web Server")
                }

                post("/rpc") {
                    val jsonPayload = call.receiveText()
                    logger.info { "${call.request.userAgent()} sent a RPC request: $jsonPayload" }
                    val response = when (val request = Json.decodeFromString<SparklyBungeeRequest>(jsonPayload)) {
                        is SparklyBungeeRequest.GetOnlinePlayersRequest -> SparklyBungeeResponse.GetOnlinePlayersResponse(
                            velocityServer.allServers.associate {
                                it.serverInfo.name to it.playersConnected.map { player ->
                                    SparklyBungeeResponse.GetOnlinePlayersResponse.ProxyPlayer(
                                        player.username,
                                        player.effectiveLocale?.toString() ?: "???",
                                        player.ping.toInt(),
                                        false
                                    )
                                }
                            }
                        )

                        is SparklyBungeeRequest.TransferPlayersRequest -> {
                            val serverInfo = when (val target = request.transferTarget) {
                                is SparklyBungeeRequest.TransferPlayersRequest.TransferTarget.BungeeServerAddressTarget -> {
                                    TODO("You cannot move to a non-registered server!")
                                }
                                is SparklyBungeeRequest.TransferPlayersRequest.TransferTarget.BungeeServerNameTarget -> {
                                    velocityServer.getServer(target.name).get()
                                }
                            }

                            val players = request.playerIds.mapNotNull { velocityServer.getPlayer(UUID.fromString(it)).getOrNull() }

                            for (player in players) {
                                player.createConnectionRequest(serverInfo).fireAndForget()
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
            logger.warn { "HTTP Server wasn't started, so we won't stop it..." }
        }
    }
}