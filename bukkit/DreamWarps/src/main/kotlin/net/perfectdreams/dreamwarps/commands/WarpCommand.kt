package net.perfectdreams.dreamwarps.commands

import net.perfectdreams.dreamcore.utils.TextUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.extensions.playTeleportEffects
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamwarps.DreamWarps

class WarpCommand(val m: DreamWarps) : SparklyCommandDeclarationWrapper {
	override fun declaration() = sparklyCommand(listOf("warp", "warps", "dwarps")) {
		executor = WarpExecutor(m)
	}

	class WarpExecutor(val m: DreamWarps) : SparklyCommandExecutor() {
		inner class Options : CommandOptions() {
			val warpName = optionalGreedyString("warp_names") { context, builder ->
				for (warp in m.warps) {
					builder.suggest(warp.name)
				}
			}
		}

		override val options = Options()

		override fun execute(context: CommandContext, args: CommandArguments) {
			val player = context.requirePlayer()
			val name = args[options.warpName]

			if (name == null) {
				m.warpsMenu.sendTo(player)
				return
			}

			val warp = m.warps.firstOrNull { it.name.equals(name, true) }

			if (warp == null) {
				context.sendMessage("${DreamWarps.PREFIX} §cNão existe nenhuma warp chamada ${name}!")
				return
			}

			if (!player.hasPermission("dreamwarps.warp.$name")) {
				player.sendMessage("${DreamWarps.PREFIX} $withoutPermission")
				return
			}

			player.teleportAsync(warp.location).thenRun {
				player.playTeleportEffects()
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
}