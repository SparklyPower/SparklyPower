package net.perfectdreams.pantufa.utils.extensions

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.pantufa.dao.User
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.pantufa
import net.perfectdreams.pantufa.tables.Users
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentMap

private val cachedUsernames: ConcurrentMap<UUID, String> = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofHours(6))
    .build<UUID, String>()
    .asMap()

suspend fun String.uuid(): UUID = pantufa.retrieveMinecraftUserFromUsername(this)?.id?.value ?: UUID.randomUUID()

val UUID.username get() = cachedUsernames[this] ?: pantufa.getMinecraftUserFromUniqueId(this).let {
    val name = it?.username ?: toString()
    cachedUsernames[this] = name
    return@let name
}