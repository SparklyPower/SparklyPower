package net.perfectdreams.pantufa

import net.perfectdreams.pantufa.threads.CheckDreamPresenceTask
import net.perfectdreams.pantufa.threads.SyncRolesTask
import net.perfectdreams.pantufa.threads.UpdateCachedGraphs
import net.perfectdreams.pantufa.threads.UpdatePantufaDiscordActivityTask
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PantufaTasks(private val pantufa: PantufaBot) {
    private val executorService = Executors.newScheduledThreadPool(4)

    fun start() {
        executorService.scheduleWithFixedDelay(
            UpdateCachedGraphs(pantufa),
            0L,
            1L,
            TimeUnit.MINUTES
        )

        executorService.scheduleWithFixedDelay(
            CheckDreamPresenceTask(),
            0L,
            1L,
            TimeUnit.MINUTES
        )

        executorService.scheduleWithFixedDelay(
            SyncRolesTask(),
            0L,
            1L,
            TimeUnit.MINUTES
        )

        executorService.scheduleWithFixedDelay(
            UpdatePantufaDiscordActivityTask(pantufa, pantufa.jda),
            0L,
            15L,
            TimeUnit.SECONDS
        )
    }

    fun shutdown() {
        executorService.shutdown()
    }
}