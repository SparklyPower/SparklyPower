package net.perfectdreams.dreamfusca.utils

import com.comphenix.packetwrapper.WrapperPlayClientSteerVehicle
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.perfectdreams.dreamcore.utils.LocationUtils
import net.perfectdreams.dreamfusca.DreamFusca
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.Minecart

class CarHandlerPacketAdapter(val m: DreamFusca) : PacketAdapter(m,
	ListenerPriority.NORMAL, // Listener priority
	PacketType.Play.Client.STEER_VEHICLE) {

	override fun onPacketSending(event: PacketEvent) {
	}

	fun yawToFace(yaw: Float): BlockFace {
		val roundedYaw = (yaw.toInt() + 360) % 360
		return if (roundedYaw in 45..134)
			BlockFace.WEST
		else if (roundedYaw in 135..224)
			BlockFace.NORTH
		else if (roundedYaw in 225..315)
			BlockFace.EAST
		else BlockFace.SOUTH
	}

	override fun onPacketReceiving(event: PacketEvent) {
		val vehicle = event.player.vehicle ?: return

		if (event.player.isInsideVehicle && vehicle.type == EntityType.MINECART && m.cars.contains(vehicle.uniqueId)) {
			val steerVehiclePacket = WrapperPlayClientSteerVehicle(event.packet)
			val minecart = vehicle as Minecart
			minecart.maxSpeed = 100.0
			var velocity = event.player.location.direction.setY(0)
			val blockBelow = minecart.location.block.getRelative(BlockFace.DOWN)

			val isRoad = isRoad(blockBelow)

			if (steerVehiclePacket.forward > 0.0f) {
				if (isRoad) {
					velocity = velocity.multiply(1.20)
				} else {
					velocity = velocity.multiply(0.25)
				}
			} else if (0.0f > steerVehiclePacket.forward) {
				velocity = velocity.multiply(-0.25)
			} else {
				velocity = vehicle.velocity // Resetar velocidade
			}

			// Players are normallyshifted a lil bit to the ground when inside a minecart
			// So we are going to shift it a lil bit
			val playerLocationShiftedALittleBitAboveTheGround = event.player.location.clone().add(0.0, 0.5, 0.0)

			val blockFace = yawToFace(playerLocationShiftedALittleBitAboveTheGround.yaw)
			val inFrontOf = playerLocationShiftedALittleBitAboveTheGround.block.getRelative(blockFace).type

			if (inFrontOf.name.contains("SLAB")) {
				velocity = velocity.setY(velocity.y + 0.5)
			} else if (blockBelow.type == Material.AIR) {
				velocity = velocity.setY(velocity.y - 0.5)
			}

			val sendParticles = vehicle.velocity != velocity
			vehicle.velocity = velocity

			if (sendParticles) {
				vehicle.world.spawnParticle(
					Particle.CAMPFIRE_COSY_SMOKE,
					vehicle.location.clone().add(
						velocity.multiply(-2)
					),
					0,
					0.0,
					0.01,
					0.0
				)
			}
		}
	}

	fun isRoad(block: Block) = block.type in m.blocks ||
			block.getRelative(BlockFace.SOUTH).type in m.blocks ||
			block.getRelative(BlockFace.NORTH).type in m.blocks ||
			block.getRelative(BlockFace.EAST).type in m.blocks ||
			block.getRelative(BlockFace.WEST).type in m.blocks
}