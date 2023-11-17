package net.perfectdreams.dreamcore.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import java.util.ArrayList
import kotlin.Comparator
import kotlin.math.roundToInt

object LocationUtils {
	val axis = arrayOf(BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH)
	val radial = arrayOf(BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST)
	const val RADIUS = 3
	var VOLUME = arrayOf<Vector3D>()

	init {
		val pos = ArrayList<Vector3D>()
		for (x in -RADIUS..RADIUS) {
			for (y in -RADIUS..RADIUS) {
				for (z in -RADIUS..RADIUS) {
					pos.add(Vector3D(x, y, z))
				}
			}
		}
		pos.sortWith(
				Comparator {
					a, b -> a.x * a.x + a.y * a.y + a.z * a.z - (b.x * b.x + b.y * b.y + b.z * b.z)
				}
		)

		VOLUME = pos.toTypedArray()
	}
	fun locationToFace(location: Location): BlockFace {
		return yawToFace(location.yaw)
	}

	fun yawToFace(yaw: Float): BlockFace {
		return yawToFace(yaw, true)
	}

	fun yawToFace(yaw: Float, useSubCardinalDirections: Boolean): BlockFace {
		return if (useSubCardinalDirections) {
			radial[Math.round(yaw / 45f) and 0x7]
		} else {
			axis[Math.round(yaw / 90f) and 0x3]
		}
	}

	fun center(location: Location): Location {
		val loc = location.clone()
		loc.x = location.blockX + 0.5
		loc.y = location.blockY + 0.5
		loc.z = location.blockZ + 0.5
		return loc
	}

	fun level(location: Location): Location {
		val loc = location.clone()
		loc.pitch = 0.0f
		return loc
	}

	fun straighten(location: Location): Location {
		val loc = location.clone()
		loc.yaw = Math.round(location.yaw / 90.0f) * 90.0f
		return loc
	}

	fun faceEntity(fromLocation: Location, at: Entity): Location {
		return if (fromLocation.world !== at.world) {
			fromLocation
		} else faceLocation(fromLocation, at.location)
	}

	fun faceLocation(fromLocation: Location, to: Location): Location {
		if (fromLocation.world !== to.world) {
			return fromLocation
		}
		val xDiff = to.x - fromLocation.x
		val yDiff = to.y - fromLocation.y
		val zDiff = to.z - fromLocation.z
		val distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff)
		val distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff)
		var yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ)) - 180.0
		val pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90.0
		if (zDiff < 0.0) {
			yaw += Math.abs(yaw) * 2.0
		}
		yaw += 90.0
		fromLocation.pitch = pitch.toFloat()
		fromLocation.yaw = yaw.toFloat()
		return fromLocation
	}

	fun isLocationBetweenLocations(location: Location, loc1: Location, loc2: Location): Boolean {
		if (loc1.world != loc2.world) // If both locations are in different worlds, then the target location will never be between them
			return false
		if (location.world != loc1.world)
			return false

		val minX = Math.min(loc1.x, loc2.x)
		val minY = Math.min(loc1.y, loc2.y)
		val minZ = Math.min(loc1.z, loc2.z)
		val maxX = Math.max(loc1.x, loc2.x)
		val maxY = Math.max(loc1.y, loc2.y)
		val maxZ = Math.max(loc1.z, loc2.z)
		return location.x in minX..maxX && location.y in minY..maxY && location.z in minZ..maxZ
	}

	fun isBlockAboveAir(world: World, x: Int, y: Int, z: Int): Boolean {
		return if (y > world.maxHeight) {
			true
		} else MaterialUtils.HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).getType())
	}

	fun isBlockLocationDisallowed(world: World, x: Int, y: Int, z: Int): Boolean {
		// TODO: Maybe don't hardcode this?
		return world.name == "Nether" && y >= 128
	}

	fun isBlockUnsafe(world: World, x: Int, y: Int, z: Int) = isBlockDamaging(world, x, y, z) || isBlockAboveAir(world, x, y, z) || isBlockLocationDisallowed(world, x, y, z)

	fun isBlockDamaging(world: World, x: Int, y: Int, z: Int): Boolean {
		val below = world.getBlockAt(x, y - 1, z)
		val type = below?.type
		return when {
			type == Material.LAVA || type == Material.LAVA -> true
			type == Material.FIRE -> true
			type?.name?.endsWith("_BED") ?: false -> true
			world.getBlockAt(x, y, z)?.type == Material.NETHER_PORTAL -> true
			world.getBlockAt(x, y, z)?.type == Material.END_PORTAL -> true
			else -> !MaterialUtils.HOLLOW_MATERIALS.contains(world.getBlockAt(x, y, z)?.type) || !MaterialUtils.HOLLOW_MATERIALS.contains(world.getBlockAt(x, y + 1, z)?.type)
		}
	}

	fun getRoundedDestination(loc: Location): Location {
		val world = loc.world
		val x = loc.blockX
		val y = loc.y.roundToInt()
		val z = loc.blockZ
		return Location(world, x + 0.5, y.toDouble(), z + 0.5, loc.yaw, loc.pitch)
	}

	fun getSafeDestination(loc: Location): Location {
		val world = loc.world
		var x = loc.blockX
		var y = loc.y.roundToInt()
		var z = loc.blockZ
		val origX = x
		val origY = y
		val origZ = z
		while (isBlockAboveAir(world, x, y, z)) {
			y -= 1
			if (y < 0) {
				y = origY
				break
			}
		}
		if (isBlockUnsafe(world, x, y, z)) {
			x = if (Math.round(loc.getX()).toInt() == origX) x - 1 else x + 1
			z = if (Math.round(loc.getZ()).toInt() == origZ) z - 1 else z + 1
		}
		var i = 0
		while (isBlockUnsafe(world, x, y, z)) {
			i++
			if (i >= VOLUME.size) {
				x = origX
				y = origY + RADIUS
				z = origZ
				break
			}
			x = origX + VOLUME[i].x
			y = origY + VOLUME[i].y
			z = origZ + VOLUME[i].z
		}
		while (isBlockUnsafe(world, x, y, z)) {
			y += 1
			if (y >= world.maxHeight) {
				x += 1
				break
			}
		}
		while (isBlockUnsafe(world, x, y, z)) {
			y -= 1
			if (y <= 1) {
				x += 1
				y = world.getHighestBlockYAt(x, z)
				if (x - 48 > loc.blockX) {
					throw HoleInFloorException()
				}
			}
		}
		return Location(world, x + 0.5, y.toDouble(), z + 0.5, loc.yaw, loc.pitch)
	}

	fun shouldFly(loc: Location): Boolean {
		val world = loc.world
		val x = loc.blockX
		var y = loc.y.roundToInt()
		val z = loc.blockZ
		var count = 0
		while (isBlockUnsafe(world, x, y, z) && y > -1) {
			y--
			count++
			if (count > 2) {
				return true
			}
		}

		return y < 0
	}

	class HoleInFloorException : IllegalArgumentException()
}