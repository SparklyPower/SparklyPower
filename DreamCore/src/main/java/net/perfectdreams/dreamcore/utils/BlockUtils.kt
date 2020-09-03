package net.perfectdreams.dreamcore.utils

import net.minecraft.server.v1_16_R2.BlockPosition
import net.minecraft.server.v1_16_R2.World
import net.minecraft.server.v1_16_R2.WorldServer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_16_R2.util.CraftMagicNumbers
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import java.util.*

object BlockUtils {
	fun attachWallSignAt(l: Location): Block? {
		l.block.type = Material.OAK_WALL_SIGN
		val s = l.block.state as Sign
		val matSign = org.bukkit.material.Sign(Material.OAK_WALL_SIGN)
		val bf = FaceUtils.getBlockFaceForWallSign(s.block) ?: run {
			l.block.type = Material.AIR
			return null
		}
		matSign.data = FaceUtils.fromBlockFaceToWallSignByte(bf)
		s.data = matSign
		s.update()
		return s.block
	}

	fun getDrops(block: Block, entity: Entity?, itemStack: ItemStack): List<ItemStack> {
		val nmsWorld = (block.world as CraftWorld).handle
		val nmsBlock = CraftMagicNumbers.getBlock(block.type)

		return net.minecraft.server.v1_16_R2.Block.getDrops(nmsBlock.blockData, nmsWorld, BlockPosition(block.x, block.y, block.z), null, (entity as? CraftEntity)?.handle, CraftItemStack.asNMSCopy(itemStack))
			.map {
				it.bukkitStack
			}
	}

	@Deprecated("NMS now uses ItemStacks")
	fun getExpCount(block: Block, fortuneLevel: Int): Int {
		return getExpCount(block, ItemStack(Material.DIAMOND_PICKAXE).apply { this.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, fortuneLevel) })
	}

	fun getExpCount(block: Block, itemStack: ItemStack): Int {
		val nmsBlock = CraftMagicNumbers.getBlock(block.type)
		val nmsWorld = (block.world as CraftWorld).handle
		return nmsBlock.getExpDrop(nmsBlock.blockData, nmsWorld as WorldServer, BlockPosition(block.x, block.y, block.z), CraftItemStack.asNMSCopy(itemStack))
	}

	fun getDropType(block: Block): Material {
		return getDropType(block.type)
	}

	fun getDropType(material: Material): Material {
		return if (material == Material.COAL_ORE) Material.COAL else if (material == Material.DIAMOND_ORE) Material.DIAMOND else if (material == Material.LAPIS_ORE) Material.LAPIS_LAZULI else if (material == Material.EMERALD_ORE) Material.EMERALD else if (material == Material.NETHER_QUARTZ_ORE) Material.QUARTZ else if (material == Material.STONE) Material.COBBLESTONE else if (material == Material.REDSTONE_ORE) Material.REDSTONE else material
	}

	fun getBlocksFromTwoLocations(loc1: Location, loc2: Location): List<Block> {
		val blocks = ArrayList<Block>()

		val topBlockX = if (loc1.blockX < loc2.blockX) loc2.blockX else loc1.blockX
		val bottomBlockX = if (loc1.blockX > loc2.blockX) loc2.blockX else loc1.blockX

		val topBlockY = if (loc1.blockY < loc2.blockY) loc2.blockY else loc1.blockY
		val bottomBlockY = if (loc1.blockY > loc2.blockY) loc2.blockY else loc1.blockY

		val topBlockZ = if (loc1.blockZ < loc2.blockZ) loc2.blockZ else loc1.blockZ
		val bottomBlockZ = if (loc1.blockZ > loc2.blockZ) loc2.blockZ else loc1.blockZ

		for (x in bottomBlockX..topBlockX) {
			for (z in bottomBlockZ..topBlockZ) {
				for (y in bottomBlockY..topBlockY) {
					val block = loc1.world.getBlockAt(x, y, z)
					blocks.add(block)
				}
			}
		}
		return blocks
	}
}