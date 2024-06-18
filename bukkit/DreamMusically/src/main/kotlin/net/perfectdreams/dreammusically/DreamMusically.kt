package net.perfectdreams.dreammusically

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.command
import net.perfectdreams.dreamcore.utils.extensions.meta
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import net.perfectdreams.dreammusically.utils.MusicPack
import net.perfectdreams.dreammusically.utils.Song
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.SoundCategory
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class DreamMusically : KotlinPlugin(), Listener {
	val world by lazy { Bukkit.getWorld("world")!! }
	val spawn by lazy { Location(world, 403.5, 65.0, 257.5) }

	override fun softEnable() {
		super.softEnable()

		/* schedule {
			while (true) {
				val song = Song.songs.random()

				for (player in world.getPlayers()) {
					if (10000 >= player.location.distanceSquared(spawn)) {
						player.playSound(Location(world, 403.5, 65.0, 257.5), song.soundName, SoundCategory.RECORDS, 6f, 1f)
						player.sendMessage("§8[§a♫§8]§e Tocando §c${song.name}§e!")
					}
				}

				// Bukkit.broadcastMessage(song.name)

				// É melhor esperar (duração + 1) para evitar problemas
				waitFor((song.duration + 1) * 20)
			}
		} */

		// Loja
		launchMainThread {
			var playJj2 = true

			while (true) {
				var str = if (playJj2) {
					"perfectdreams.sfx.jj3_shop"
				} else {
					"perfectdreams.sfx.buymode3"
				}

				val world = Bukkit.getWorld("world")
				if (world != null) {
					for (player in world.getPlayers()) {
						player.playSound(Location(world, 526.5, 65.0, 257.5), str, SoundCategory.RECORDS, 3f, 1f)
					}
				}

				if (playJj2) {
					delayTicks(1420)
				} else {
					delayTicks(3360)
				}
				playJj2 = !playJj2
			}
		}
	}
}