package net.perfectdreams.pantufa.threads

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.pantufa.PantufaBot
import java.time.Instant

class UpdateCachedGraphs(private val pantufa: PantufaBot) : Runnable {
    companion object {
        val logger = KotlinLogging.logger {}
    }

    override fun run() {
        try {
            val now = Instant.now()
            val nowOneHourAgo = now.minusSeconds(60 * 60)
            runBlocking {
                pantufa.playersOnlineGraph.updateGraph(nowOneHourAgo, now)
            }
        } catch (e: Throwable) {
            logger.warn(e) { "Something went wrong while updating graphs!" }
        }
    }
}