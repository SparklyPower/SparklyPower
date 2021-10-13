package net.perfectdreams.sparklydreamer.utils.metrics

import io.prometheus.client.Gauge
import org.bukkit.Bukkit

class LoadedChunksMetric : Metric() {
    private val LOADED_CHUNKS: Gauge = Gauge.build()
        .name("loaded_chunks")
        .help("Chunks loaded per world")
        .labelNames("world")
        .create()
        .register()

    override fun doCollect() {
        for (world in Bukkit.getWorlds())
            LOADED_CHUNKS.labels(world.name).set(world.loadedChunks.size.toDouble())
    }
}