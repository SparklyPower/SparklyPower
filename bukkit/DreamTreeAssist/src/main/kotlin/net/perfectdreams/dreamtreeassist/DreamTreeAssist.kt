package net.perfectdreams.dreamtreeassist

import com.okkero.skedule.schedule
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamtreeassist.listeners.PlayerListener
import net.perfectdreams.dreamtreeassist.utils.BlockLocation
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Item
import org.bukkit.event.Listener
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue

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

					for (droppedItem in droppedItems) {
						if (droppedItem.ticksLived >= (20 * 15) && droppedItem.itemStack.type in saplings && !droppedItem.hasMetadata("checkedTree")) { // 15s
							val blockAtDrop = droppedItem.location.block

							droppedItem.setMetadata("checkedTree", FixedMetadataValue(this@DreamTreeAssist, true))

							if (blockAtDrop.type == Material.AIR && (blockAtDrop.getRelative(BlockFace.DOWN).type == Material.GRASS_BLOCK || blockAtDrop.getRelative(BlockFace.DOWN).type == Material.DIRT)) {
								val claim = GriefPrevention.instance.dataStore.getClaimAt(blockAtDrop.location, false, null)

								if (claim != null) // Do not transform into sapling if it is a protected terrain
									continue

								blockAtDrop.type = droppedItem.itemStack.type

								if (droppedItem.itemStack.amount == 1)
									droppedItem.remove()
								else
									droppedItem.itemStack.amount -= 1
							}
						}
					}
				}

				waitFor(20L)
			}
		}
	}
}