package net.perfectdreams.sparklydreamer.utils.metrics

import java.time.Duration

/**
 * Used to hold Prometheus instrumentations
 */
object Prometheus {
    val UPDATE_PERIOD = Duration.ofSeconds(5L)

    val metrics = listOf(
        OnlinePlayersMetric(),
        TPSMetric(),
        LoadedChunksMetric(),
        LoadedEntitiesMetric()
    )

    init {
        JFRExports.register()
    }
}