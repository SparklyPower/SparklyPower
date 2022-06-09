package net.perfectdreams.dreamcorreios.utils

import org.bukkit.inventory.ItemStack

class CaixaPostalAccessHolder(
    val m: CaixaPostal
) {
    val items by m::items

    fun addItems(vararg itemStacks: ItemStack) = m.addItems(*itemStacks)

    suspend fun release() = m.releaseAccess(this)
}