package net.perfectdreams.dreammochilas.listeners

import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.balance
import net.perfectdreams.dreamcore.utils.extensions.getStoredMetadata
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreammochilas.DreamMochilas
import net.perfectdreams.dreammochilas.dao.Mochila
import net.perfectdreams.dreammochilas.tables.Mochilas
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.jetbrains.exposed.sql.transactions.transaction

class UpgradeSizeSignListener(val m: DreamMochilas) : Listener {
    private val savingNewMocilhaStatus = mutableListOf<Long>()

    private val signs = listOf(
        SignBlockLocation(528, 65, 257),
        SignBlockLocation(523, 65, 257),
        SignBlockLocation(530, 65, 257),
        SignBlockLocation(535, 65, 257)
    )

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    fun onInteract(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return

        if (!clickedBlock.type.name.contains("OAK_WALL"))
            return

        if (!signs.any { clickedBlock.x == it.x && clickedBlock.y == it.y && clickedBlock.z == it.z })
            return

        val item = e.player.inventory.itemInHand

        val isMochila = item.getStoredMetadata("isMochila")?.toBoolean() ?: false
        e.isCancelled = true

        if (isMochila) {
            if (2_000 > e.player.balance) {
                e.player.sendMessage("§cVocê precisa ter 2000 sonecas para poder fazer um upgrade maroto na sua mochila!")
                return
            }

            val mochilaId = item.getStoredMetadata("mochilaId")?.toLong() ?: return

            if (savingNewMocilhaStatus.contains(mochilaId))
                return

            savingNewMocilhaStatus.add(mochilaId)
            m.launchAsyncThread {
                // We don't *need* to globally lock interactions with the mochila because we are only updating its size!
                val success = transaction(Databases.databaseNetwork) {
                    val mochila = Mochila.find { Mochilas.id eq mochilaId }
                        .firstOrNull() ?: return@transaction false

                    if (mochila.size == 54) {
                        e.player.sendMessage("§cSua mochila já está no máximo permitido!")
                        return@transaction false
                    }

                    mochila.size += 9
                    return@transaction true
                }

                if (success) {
                    onMainThread {
                        e.player.balance -= 2000
                        e.player.sendMessage("§aProntinho!")
                        savingNewMocilhaStatus.remove(mochilaId)
                    }
                }
            }
        } else {
            e.player.sendMessage("§cIsto não me parece ser uma mochila...")
        }
    }

    data class SignBlockLocation(
        val x: Int,
        val y: Int,
        val z: Int
    )
}