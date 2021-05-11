package net.perfectdreams.dreamcore.commands

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.isPrimaryThread
import net.perfectdreams.dreamcore.utils.scheduler.BukkitDispatcher
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread

object DreamCoreCommand : DSLCommandBase<DreamCore> {
    override fun command(plugin: DreamCore) = create(listOf("dreamcore")) {
        permission = "dreamcore.setup"

        executes {
            sender.sendMessage("§aDreamCore! Let's make the world a better place, one plugin at a time")
        }
    }
}