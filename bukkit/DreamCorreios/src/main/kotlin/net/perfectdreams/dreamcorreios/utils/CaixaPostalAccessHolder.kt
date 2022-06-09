package net.perfectdreams.dreamcorreios.utils

import org.bukkit.inventory.ItemStack

class CaixaPostalAccessHolder(
    val m: CaixaPostal
) {
    val items by m::items

    fun addItem(vararg itemStacks: ItemStack) = m.addItem(*itemStacks)

    suspend fun release() = m.releaseAccess(this)
}