package net.perfectdreams.dreammini.utils

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.adventure.sendTextComponent
import net.perfectdreams.dreamcore.utils.extensions.isWithinRegion
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class DontDropItemsInSpawnListener(val m: DreamMini) : Listener {
	@EventHandler
	fun onDrop(e: PlayerDropItemEvent) {
		if (e.player.location.isWithinRegion("spawn") && !e.player.hasPermission("dreammini.bypassdropblock")) {
			e.isCancelled = true
			e.player.sendTextComponent {
				color(NamedTextColor.RED)
				append("Você não pode jogar itens fora no spawn! Se você deseja jogar suas tralhas no lixo, então use ")
				appendCommand("/lixeira")
				append("!")
			}
		}
	}
}