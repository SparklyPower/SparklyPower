package net.perfectdreams.dreamcore.utils.scheduler

import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import kotlin.coroutines.CoroutineContext

@OptIn(InternalCoroutinesApi::class)
class BukkitDispatcher(val plugin: JavaPlugin, val async: Boolean = false) : CoroutineDispatcher(), Delay {
    private val bukkitScheduler = Bukkit.getScheduler()

    private val runTaskLater: (Plugin, Runnable, Long) -> BukkitTask =
        if (async)
            bukkitScheduler::runTaskLaterAsynchronously
        else
            bukkitScheduler::runTaskLater
    private val runTask: (Plugin, Runnable) -> BukkitTask =
        if (async)
            bukkitScheduler::runTaskAsynchronously
        else
            bukkitScheduler::runTask

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val task = runTaskLater(
            plugin,
            Runnable {
                continuation.apply { resumeUndispatched(Unit) }
            },
            timeMillis / 50)
        continuation.invokeOnCancellation { task.cancel() }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!context.isActive) {
            return
        }

        if (!async && Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            runTask(plugin, block)
        }
    }
}