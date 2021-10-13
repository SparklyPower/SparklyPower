package net.perfectdreams.sparklydreamer.utils

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.sparklydreamer.SparklyDreamer
import net.perfectdreams.sparklydreamer.utils.metrics.Prometheus
import java.io.StringWriter

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

                get("/api/server/metrics") {
                    // We can't use "onMainThread" here because the thread local variable isn't set, so don't use it!
                    val deferred = plugin.launchMainThreadDeferred {
                        // Collect Metrics
                        Prometheus.metrics.forEach { it.doCollect() }
                    }

                    deferred.await() // Await all metrics to be collected

                    val writer = StringWriter()

                    withContext(Dispatchers.IO) {
                        // Gets all registered Prometheus Metrics and writes to the StringWriter
                        TextFormat.write004(
                            writer,
                            CollectorRegistry.defaultRegistry.metricFamilySamples()
                        )
                    }

                    call.respondText(writer.toString())
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