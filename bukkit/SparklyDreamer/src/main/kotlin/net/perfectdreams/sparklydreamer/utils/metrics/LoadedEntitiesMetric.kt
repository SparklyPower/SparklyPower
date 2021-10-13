package net.perfectdreams.sparklydreamer.utils.metrics

import io.prometheus.client.Gauge
import org.bukkit.Bukkit

class LoadedEntitiesMetric : Metric() {
    private val LOADED_ENTITIES: Gauge = Gauge.build()
        .name("loaded_entities")
        .help("Entities loaded per world and type")
        .labelNames("world", "entity")
        .create()
        .register()

    override fun doCollect() {
        for (world in Bukkit.getWorlds())
            for ((entityType, entities) in world.entities.groupBy { it.type })
                LOADED_ENTITIES.labels(
                    world.name,
                    entityType.key.key
                ).set(entities.size.toDouble())
    }
}