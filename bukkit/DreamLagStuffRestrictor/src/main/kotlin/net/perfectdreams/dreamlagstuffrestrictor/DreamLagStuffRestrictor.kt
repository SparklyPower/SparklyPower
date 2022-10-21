package net.perfectdreams.dreamlagstuffrestrictor

import kotlinx.coroutines.delay
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamlagstuffrestrictor.commands.EntitySearchKillOutsideExecutor
import net.perfectdreams.dreamlagstuffrestrictor.commands.declarations.EntitySearchDeclaration
import net.perfectdreams.dreamlagstuffrestrictor.listeners.BlockLaggyBlocksListener
import net.perfectdreams.dreamlagstuffrestrictor.utils.ThanosSnap
import org.bukkit.Bukkit
import org.bukkit.event.Listener

class DreamLagStuffRestrictor : KotlinPlugin(), Listener {
	override fun softEnable() {
		super.softEnable()

		registerEvents(BlockLaggyBlocksListener())
		registerCommand(EntitySearchDeclaration())

		ThanosSnap(this).start()

		this.launchMainThread {
			while (true) {
				val w = Bukkit.getWorld("world")

				if (w != null) {
					var killed = 0
					w.entities.forEach {
						if (it.type in EntitySearchKillOutsideExecutor.killTypes) {
							val claim = GriefPrevention.instance.dataStore.getClaimAt(it.location, true, null)

							if (claim == null) {
								killed++
								it.remove()
							}
						}
					}

					logger.info { "Killed $killed mobs!" }
				}

				delay(60_000)
			}
		}
	}

	override fun softDisable() {
		super.softDisable()
	}
}