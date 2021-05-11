package net.perfectdreams.dreamcore.utils.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import kotlin.coroutines.resume

/**
 * Delays the current coroutine by [ticks], the plugin is inferred from the [KotlinPlugin.PLUGIN_TASK_THREAD_LOCAL] variable
 *
 * @param ticks how many ticks the task should wait
 */
suspend fun delayTicks(ticks: Long) = delayTicks(KotlinPlugin.PLUGIN_TASK_THREAD_LOCAL.get(), ticks)

/**
 * Delays the current coroutine by [ticks]
 *
 * @param plugin the current plugin
 * @param ticks  how many ticks the task should wait
 */
suspend fun delayTicks(plugin: Plugin, ticks: Long) {
    suspendCancellableCoroutine<Unit> { cont ->
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { cont.resume(Unit) }, ticks)
    }
}

/**
 * Switches the context to the server's main thread and executes [block], the plugin is inferred from the [KotlinPlugin.PLUGIN_TASK_THREAD_LOCAL] variable
 *
 * @param block what should be executed
 */
suspend fun <T> onMainThread(block: suspend CoroutineScope.() -> T) = onMainThread(KotlinPlugin.PLUGIN_TASK_THREAD_LOCAL.get(), block)

/**
 * Switches the context to the server's async thread and executes [block], the plugin is inferred from the [KotlinPlugin.PLUGIN_TASK_THREAD_LOCAL] variable
 *
 * @param block what should be executed
 */
suspend fun <T> onAsyncThread(block: suspend CoroutineScope.() -> T) = onAsyncThread(KotlinPlugin.PLUGIN_TASK_THREAD_LOCAL.get(), block)

/**
 * Switches the context to the server's main thread and executes [block]
 *
 * @param plugin the current plugin
 * @param block  what should be executed
 */
suspend fun <T> onMainThread(plugin: JavaPlugin, block: suspend CoroutineScope.() -> T): T {
    return withContext(BukkitDispatcher(plugin, false)) {
        block.invoke(this)
    }
}

/**
 * Switches the context to the server's async thread and executes [block]
 *
 * @param plugin the current plugin
 * @param block  what should be executed
 */
suspend fun <T> onAsyncThread(plugin: JavaPlugin, block: suspend CoroutineScope.() -> T): T {
    return withContext(BukkitDispatcher(plugin, true)) {
        block.invoke(this)
    }
}