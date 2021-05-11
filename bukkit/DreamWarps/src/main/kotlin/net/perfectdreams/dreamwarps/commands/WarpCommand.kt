package net.perfectdreams.dreamwarps.commands

import net.perfectdreams.dreamcore.utils.TextUtils
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamwarps.DreamWarps
import org.bukkit.Particle
import org.bukkit.entity.Player

class WarpCommand(val m: DreamWarps) : AbstractCommand("warp", listOf("warps", "dwarps")) {
	@Subcommand
	fun root(player: Player) {
		m.warpsMenu.generateMenu(player).sendTo(player)
	}

	@Subcommand
	fun teleport(player: Player, name: String) {
		val warp = m.warps.firstOrNull { it.name.equals(name, true) }

		if (warp == null) {
			player.sendMessage("${DreamWarps.PREFIX} §cNão existe nenhuma warp chamada ${name}!")
			return
		}

		if (!player.hasPermission("dreamwarps.warp.$name")) {
			player.sendMessage("${DreamWarps.PREFIX} $withoutPermission")
			return
		}

		player.teleportAsync(warp.location).thenRun {
			player.world.spawnParticle(Particle.VILLAGER_HAPPY, player.location.add(0.0, 0.5, 0.0), 25, 0.5, 0.5, 0.5)
			player.sendMessage("${DreamWarps.PREFIX} §aVocê chegou ao seu destino. §cʕ•ᴥ•ʔ")
			player.sendTitle(
				"§b${warp.fancyName}",
				"§3${TextUtils.ROUND_TO_2_DECIMAL.format(warp.location.x)}§b, §3${TextUtils.ROUND_TO_2_DECIMAL.format(
					warp.location.y
				)}§b, §3${TextUtils.ROUND_TO_2_DECIMAL.format(warp.location.z)}",
				10,
				60,
				10
			)
		}
	}
}