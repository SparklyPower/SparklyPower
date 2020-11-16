package net.perfectdreams.dreamdemocracy

import com.okkero.skedule.schedule
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.extensions.meta
import org.bukkit.Material
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta

class DreamDemocracy : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		schedule {
			while (true) {
				val loritta = CitizensAPI.getNPCRegistry().getById(879)
				val gabriela = CitizensAPI.getNPCRegistry().getById(880)
				val pantufa = CitizensAPI.getNPCRegistry().getById(882)

				checkSantinho(loritta, 11126, "O planfleto tem cheiro de Blueberry")
				checkSantinho(gabriela, 11127, "O planfleto tem cheiro de pincel")
				checkSantinho(pantufa, 11128, "O planfleto tem cheiro de caixa de celular novo")

				waitFor(20 * 5)
			}
		}
	}

	fun checkSantinho(npc: NPC, mapId: Int, description: String) {
		val players = npc.entity.location.world.players

		forEach@for (player in players) {
			val distance = npc.entity.location.distanceSquared(player.location)

			if (20 >= distance) {
				// Drop map
				val item = ItemStack(Material.FILLED_MAP)
					.rename("§3§lPropaganda Política")
					.lore("§7§o$description")
					.meta<MapMeta> {
						this.mapId = mapId
					}

				npc.entity.location.world.dropItem(
					npc.entity.location.add(0.0, 1.0, 0.0),
					item
				).apply {
					this.velocity = npc.entity.location.direction
						.multiply(0.25)
				}
				break@forEach
			}
		}
	}
}