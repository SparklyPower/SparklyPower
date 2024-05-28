package net.perfectdreams.pantufa.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import kotlin.time.Duration

/**
 * Schedules [action] to be executed on [scope] every [period] with a [initialDelay]
 */
fun scheduleCoroutineAtFixedRate(taskName: String, scope: CoroutineScope, period: Duration, initialDelay: Duration = Duration.ZERO, action: RunnableCoroutine) {
    val logger = KotlinLogging.logger(taskName)

    scope.launch {
        delay(initialDelay)

        val mutex = Mutex()

        while (true) {
            launch {
                logger.info { "Preparing to run task - Is mutex locked? ${mutex.isLocked}" }
                mutex.withLock {
                    logger.info { "Running task..." }
                    try {
                        action.run()
                    } catch (e: Throwable) {
                        logger.warn(e) { "Uncaught error when running the task!" }
                    }
                    logger.info { "Task has finished running!" }
                }
            }
            logger.info { "Waiting $period to execute next task..." }
            delay(period)
        }
    }
}

fun interface RunnableCoroutine {
    suspend fun run()
}