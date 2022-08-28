package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamclubes.utils.toSync
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.TextUtils
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.jetbrains.exposed.sql.transactions.transaction

class HomeClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        withPlayerClube(player) { clube, selfMember ->
            val clubeHome = onAsyncThread {
                transaction(Databases.databaseNetwork) {
                    clube.home
                }
            }

            if (clubeHome != null) {
                val location = Location(
                    Bukkit.getWorld(clubeHome.worldName),
                    clubeHome.x,
                    clubeHome.y,
                    clubeHome.z,
                    clubeHome.yaw,
                    clubeHome.pitch
                )

                player.teleport(location)
                player.world.spawnParticle(
                    Particle.VILLAGER_HAPPY,
                    player.location.add(0.0, 0.5, 0.0),
                    25,
                    0.5,
                    0.5,
                    0.5
                )
                player.sendMessage("§aVocê chegou ao seu destino. §cʕ•ᴥ•ʔ")
                player.sendTitle(
                    "§b${clube.name}",
                    "§3${TextUtils.ROUND_TO_2_DECIMAL.format(location.x)}§b, §3${
                        TextUtils.ROUND_TO_2_DECIMAL.format(location.y)
                    }§b, §3${TextUtils.ROUND_TO_2_DECIMAL.format(location.z)}",
                    10,
                    60,
                    10
                )
            }
        }
    }
}