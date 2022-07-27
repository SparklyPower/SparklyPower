package net.perfectdreams.dreamcorreios.utils

import kotlinx.coroutines.sync.withLock
import org.bukkit.inventory.ItemStack

class CaixaPostalAccessHolder(
    val m: CaixaPostal
) {
    val items by m::items

    fun addItem(vararg itemStacks: ItemStack) = m.addItem(*itemStacks)

    suspend fun release() = m.m.loadingAndUnloadingCaixaPostalMutex.withLock { m.releaseAccess(this) }
}