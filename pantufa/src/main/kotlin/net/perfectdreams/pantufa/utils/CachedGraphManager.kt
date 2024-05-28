package net.perfectdreams.pantufa.utils

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.replay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.pantufa.PantufaBot
import java.time.Instant

class CachedGraphManager(val token: String, val url: String) {
    private val state = MutableStateFlow<ByteArray?>(null)

    // Get the first non null value
    suspend fun getCachedGraph(): ByteArray = state.first { it != null }!!

    suspend fun updateGraph(from: Instant, to: Instant) {
        val urlWithTime = url + "&from=${from.toEpochMilli()}&to=${to.toEpochMilli()}"
        state.value = PantufaBot.http.get(urlWithTime) {
            header("Authorization", "Bearer $token")
        }.body()
    }
}