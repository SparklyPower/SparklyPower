package net.perfectdreams.dreamcore.utils

import net.perfectdreams.dreamcore.DreamCore
import org.bukkit.Location
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.MemorySection
import kotlin.reflect.KProperty

class DreamConfig(config: Configuration) {
    val map = config.getValues(true)

    fun getSpawn(): Location {
        return DreamCore.INSTANCE.spawn!!
    }

    val serverName: String by ConfigMapDelegate(map, "PleaseSetThis")
    val bungeeName: String by ConfigMapDelegate(map, "PleaseSetThis")

    val strings: StringsConfig = map["strings"]?.let {
        StringsConfig((it as MemorySection).getValues(true))
    } ?: StringsConfig(mapOf())

    val discord: DiscordConfig? = map["discord"]?.let {
        DiscordConfig((it as MemorySection).getValues(true))
    }

    val socket: SocketConfig? = map["socket"]?.let {
        SocketConfig((it as MemorySection).getValues(true))
    }

    val networkDatabase: NetworkDatabaseConfig? = map["networkDatabase"]?.let {
        NetworkDatabaseConfig((it as MemorySection).getValues(true))
    }

    val legacyMongoDB: LegacyMongoDBConfig? = map["legacyMongoDb"]?.let {
        LegacyMongoDBConfig((it as MemorySection).getValues(true))
    }

    val blacklistedWorldsTeleport: List<String> by ConfigMapDelegate(map, listOf())
    val blacklistedRegionsTeleport: List<String> by ConfigMapDelegate(map, listOf())

    class ConfigMapDelegate<T>(val map: Map<String, Any?>, val default: T) {
        operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
            return map[prop.name] as T? ?: default
        }
    }

    class DiscordConfig(val map: Map<String, Any?>) {
        val eventAnnouncementChannelId: String? by ConfigMapDelegate(map, null)

        val webhooks: WebhooksConfig? = map["webhooks"]?.let {
            WebhooksConfig((it as MemorySection).getValues(true))
        }

        class WebhooksConfig(val map: Map<String, Any?>) {
            val warn: String? by ConfigMapDelegate(map, null)
            val info: String? by ConfigMapDelegate(map, null)
            val error: String? by ConfigMapDelegate(map, null)
        }
    }

    class StringsConfig(val map: Map<String, Any?>) {
        val withoutPermission: String by ConfigMapDelegate(map, "§cVocê não tem permissão para fazer isto!")
        val staffPermission: String by ConfigMapDelegate(map, "sparklypower.staff")
    }

    class SocketConfig(val map: Map<String, Any?>) {
        val port: Int by ConfigMapDelegate(map, 13375)
    }

    class NetworkDatabaseConfig(val map: Map<String, Any?>) {
        val type: String by ConfigMapDelegate(map, "SQLite")
        val ip: String? by ConfigMapDelegate(map, null)
        val port: String? by ConfigMapDelegate(map, null)
        val user: String? by ConfigMapDelegate(map, null)
        val password: String? by ConfigMapDelegate(map, null)
        val databaseName: String? by ConfigMapDelegate(map, "sparklypower")
        val tablePrefix: String? by ConfigMapDelegate(map, "")
    }

    class LegacyMongoDBConfig(val map: Map<String, Any?>) {
        val ip: String? by ConfigMapDelegate(map, null)
        val serverDatabaseName: String by ConfigMapDelegate(map, "sparklypower")
    }
}