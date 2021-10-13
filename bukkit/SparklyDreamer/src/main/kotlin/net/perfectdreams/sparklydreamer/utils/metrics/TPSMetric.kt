package net.perfectdreams.sparklydreamer.utils.metrics

import io.prometheus.client.Gauge
import org.bukkit.Bukkit

class TPSMetric : Metric() {
    private val TPS: Gauge = Gauge.build()
        .name("ticks_per_second")
        .help("Ticks per Second")
        .create()
        .register()

    override fun doCollect() {
        TPS.set(Bukkit.getTPS()[0])
    }
}