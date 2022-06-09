package net.perfectdreams.dreamcorreios.utils

import net.perfectdreams.dreamcorreios.DreamCorreios
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun Player.addItemIfPossibleOrAddToPlayerMailbox(vararg items: ItemStack) = DreamCorreios.getInstance().addItem(this, *items)