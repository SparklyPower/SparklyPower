package net.perfectdreams.dreamcore.utils

import net.minecraft.server.v1_16_R3.EntityFireworks
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityStatus
import net.minecraft.server.v1_16_R3.World
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class InstantFirework(world: World, location: Location) : EntityFireworks(
	world,
	location.x,
	location.y,
	location.z,
	CraftItemStack.asNMSCopy(ItemStack(Material.FIREWORK_ROCKET))
) {
	private var gone = false

	init {
		this.a(0.25f, 0.25f)
	}

	override fun tick() {
		if (gone)
			return

		gone = true
		this.world.broadcastEntityEffect(this, 17.toByte())
		this.die()
	}

	companion object {
		@Deprecated("Please use spawn(location, effect)")
		fun spawn(location: Location, effect: FireworkEffect, vararg players: Player = location.world.players.toTypedArray()) = spawn(location, effect)

		/**
		 * Spawns an instant firework at [location] with the [effect]
		 *
		 * @param location where the effect should be spawned
		 * @param effect   what effects the firework should have
		 */
		fun spawn(location: Location, effect: FireworkEffect) {
			try {
				val nmsWorld = (location.world as CraftWorld).handle
				val firework = InstantFirework(nmsWorld, location)
				val bukkitEntity = firework.bukkitEntity as Firework
				val meta = bukkitEntity.fireworkMeta
				meta.addEffect(effect)
				bukkitEntity.fireworkMeta = meta

				if (nmsWorld.addEntity(firework))
					firework.isInvisible = true
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}
}