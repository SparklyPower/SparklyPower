package net.perfectdreams.dreamcore.utils.extensions

import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

/**
 * Retorna se o usuário realmente moveu (mudou as coordenadas x, y, z) ou se apenas mexeu a cabeça
 *
 * @return se o usuário realmente se moveu
 */
val PlayerMoveEvent.displaced: Boolean
	get() = this.from.x != this.to.x || this.from.y != this.to.y || this.from.z != this.to.z

val PlayerInteractEvent.rightClick: Boolean
	get() = this.action == Action.RIGHT_CLICK_AIR || this.action == Action.RIGHT_CLICK_BLOCK

val PlayerInteractEvent.leftClick: Boolean
	get() = this.action == Action.LEFT_CLICK_AIR || this.action == Action.LEFT_CLICK_BLOCK

val PlayerInteractEvent.clickedOnBlock: Boolean
	get() = this.action == Action.RIGHT_CLICK_BLOCK || this.action == Action.LEFT_CLICK_BLOCK

val PlayerInteractEvent.clickedOnAir: Boolean
	get() = this.action == Action.LEFT_CLICK_AIR || this.action == Action.RIGHT_CLICK_AIR

val PlayerInteractEvent.physical: Boolean
	get() = this.action == Action.PHYSICAL