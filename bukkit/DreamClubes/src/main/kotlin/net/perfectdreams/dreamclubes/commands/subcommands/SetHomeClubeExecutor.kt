package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.dao.ClubeHome
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

class SetHomeClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        withPlayerClube(player) { clube, selfMember ->
            if (!selfMember.permissionLevel.canExecute(ClubePermissionLevel.ADMIN)) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão para fazer isto!")
                return@withPlayerClube
            }

            val location = player.location
            onAsyncThread {
                transaction(Databases.databaseNetwork) {
                    val oldHome = clube.home

                    val newHome = ClubeHome.new {
                        this.x = location.x
                        this.y = location.y
                        this.z = location.z
                        this.yaw = location.yaw
                        this.pitch = location.pitch
                        this.worldName = location.world.name
                    }

                    clube.home = newHome
                    oldHome?.delete()
                }
            }

            player.sendMessage("${DreamClubes.PREFIX} §aCasa do clube marcada com sucesso!")
        }
    }
}