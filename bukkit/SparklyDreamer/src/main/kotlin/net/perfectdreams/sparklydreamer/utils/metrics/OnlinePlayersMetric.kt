package net.perfectdreams.sparklydreamer.utils.metrics

import io.prometheus.client.Gauge
import org.bukkit.Bukkit

class OnlinePlayersMetric : Metric() {
    private val PLAYERS_ONLINE: Gauge = Gauge.build()
        .name("players_online_total")
        .help("Players currently online per world")
        .labelNames("world")
        .create()
        .register()

    override fun doCollect() {
        for (world in Bukkit.getWorlds())
            PLAYERS_ONLINE.labels(world.name).set(world.players.size.toDouble())
    }
}