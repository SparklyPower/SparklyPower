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
                    try {
                        entityType.key.key
                    } catch (e: IllegalArgumentException) {
                        // I'm not really sure when this can happen, but it does.
                        // "java.lang.IllegalArgumentException: EntityType doesn't have key! Is it UNKNOWN?"
                        "unknown"
                    }
                ).set(entities.size.toDouble())
    }
}