package net.perfectdreams.sparklydreamer.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamsonecas.SonecasUtils
import net.perfectdreams.dreamsonecas.tables.PlayerSonecas
import net.perfectdreams.sparklydreamer.SparklyDreamer
import net.sparklypower.rpc.SparklySurvivalRPCRequest
import net.sparklypower.rpc.SparklySurvivalRPCResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class APIServer(private val plugin: SparklyDreamer) {
    private val logger = plugin.logger
    private var server: ApplicationEngine? = null

    fun start() {
        logger.info { "Starting HTTP Server..." }
        val server = embeddedServer(Netty, port = 9999) {
            routing {
                get("/") {
                    call.respondText("SparklyPower API Web Server")
                }

                post("/rpc") {
                    val jsonPayload = call.receiveText()
                    logger.info { "${call.request.userAgent()} sent a RPC request: $jsonPayload" }

                    val request = Json.decodeFromString<SparklySurvivalRPCRequest>(jsonPayload)

                    val response = when (request) {
                        is SparklySurvivalRPCRequest.GetSonecasRequest -> {
                            val playerUniqueId = UUID.fromString(request.playerUniqueId)

                            val (money, ranking) = net.perfectdreams.exposedpowerutils.sql.transaction(Dispatchers.IO, Databases.databaseNetwork) {
                                val money = PlayerSonecas.selectAll()
                                    .where { PlayerSonecas.id eq playerUniqueId }
                                    .firstOrNull()
                                    ?.get(PlayerSonecas.money)
                                    ?.toDouble() ?: 0.0

                                val ranking = if (money > 0.0) {
                                    PlayerSonecas.selectAll().where { PlayerSonecas.money greaterEq money.toBigDecimal() }
                                        .count()
                                } else null

                                Pair(money, ranking)
                            }

                            SparklySurvivalRPCResponse.GetSonecasResponse.Success(money, ranking)
                        }

                        is SparklySurvivalRPCRequest.TransferSonecasRequest -> TODO()
                    }

                    call.respondText(
                        Json.encodeToString<SparklySurvivalRPCResponse>(response),
                        ContentType.Application.Json
                    )
                }
            }
        }

        // If set to "wait = true", the server hangs
        this.server = server.start(wait = false)
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