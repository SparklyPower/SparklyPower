package net.perfectdreams.dreamcore.utils

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace



object FaceUtils {
	val BLOCKED_ATTACHMENTS = setOf<Material>(
			Material.AIR, Material.OAK_WALL_SIGN, Material.OAK_SIGN, Material.CAKE, Material.COBWEB, Material.WATER, Material.LAVA
	)

	fun getBlockFaceForWallSign(b: Block): BlockFace? {
		if (!FaceUtils.BLOCKED_ATTACHMENTS.contains(b.getRelative(BlockFace.NORTH).type)) {
			return BlockFace.NORTH;
		}
		if (!FaceUtils.BLOCKED_ATTACHMENTS.contains(b.getRelative(BlockFace.SOUTH).type)) {
			return BlockFace.SOUTH;
		}
		if (!FaceUtils.BLOCKED_ATTACHMENTS.contains(b.getRelative(BlockFace.EAST).type)) {
			return BlockFace.EAST;
		}
		if (!FaceUtils.BLOCKED_ATTACHMENTS.contains(b.getRelative(BlockFace.WEST).type)) {
			return BlockFace.WEST;
		}
		return null;
	}

	fun fromBlockFaceToWallSignByte(bf: BlockFace): Byte {
		when (bf) {
			BlockFace.NORTH -> {
				return 3
			}
			BlockFace.SOUTH -> {
				return 2
			}
			BlockFace.WEST -> {
				return 5
			}
			BlockFace.EAST -> {
				return 4
			}
			else -> {
				return 0
			}
		}
	}

	fun fromBlockFaceToSignByte(bf: BlockFace): Byte {
		when (bf) {
			BlockFace.DOWN -> {
				return 0
			}
			BlockFace.EAST -> {
				return 12
			}
			BlockFace.EAST_NORTH_EAST -> {
				return 11
			}
			BlockFace.EAST_SOUTH_EAST -> {
				return 13
			}
			BlockFace.NORTH -> {
				return 8
			}
			BlockFace.NORTH_EAST -> {
				return 10
			}
			BlockFace.NORTH_NORTH_EAST -> {
				return 9
			}
			BlockFace.NORTH_NORTH_WEST -> {
				return 7
			}
			BlockFace.NORTH_WEST -> {
				return 6
			}
			BlockFace.SELF -> {
				return 0
			}
			BlockFace.SOUTH -> {
				return 0
			}
			BlockFace.SOUTH_EAST -> {
				return 14
			}
			BlockFace.SOUTH_SOUTH_EAST -> {
				return 15
			}
			BlockFace.SOUTH_SOUTH_WEST -> {
				return 1
			}
			BlockFace.SOUTH_WEST -> {
				return 2
			}
			BlockFace.UP -> {
				return 0
			}
			BlockFace.WEST -> {
				return 4
			}
			BlockFace.WEST_NORTH_WEST -> {
				return 3
			}
			BlockFace.WEST_SOUTH_WEST -> {
				return 3
			}
			else -> {
				return 0
			}
		}
	}
}