package net.perfectdreams.dreamtreeassist

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamtreeassist.listeners.PlayerListener
import net.perfectdreams.dreamtreeassist.utils.BlockLocation
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Item
import org.bukkit.event.Listener

class DreamTreeAssist : KotlinPlugin(), Listener {
	private val saplings = listOf(
		Material.ACACIA_SAPLING,
		Material.SPRUCE_SAPLING,
		Material.DARK_OAK_SAPLING,
		Material.BIRCH_SAPLING,
		Material.OAK_SAPLING,
		Material.JUNGLE_SAPLING
	)

	val placedLogs = mutableSetOf<BlockLocation>()

	override fun softEnable() {
		super.softEnable()

		registerEvents(PlayerListener(this))

		schedule {
			while (true) {
				val defaultWorld = Bukkit.getWorld("world")

				if (defaultWorld != null) {
					val droppedItems = defaultWorld.getEntitiesByClass(Item::class.java)

					droppedItems.forEach {
						if (it.ticksLived >= (20 * 15) && it.itemStack.type in saplings) { // 15s
							val blockAtDrop = it.location.block

							if (blockAtDrop.type == Material.AIR && (blockAtDrop.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK || blockAtDrop.getRelative(BlockFace.DOWN).type == Material.DIRT)) {
								blockAtDrop.type = it.itemStack.type

								if (it.itemStack.amount == 1)
									it.remove()
								else
									it.itemStack.amount -= 1
							}
						}
					}
				}

				waitFor(20L)
			}
		}
	}
}