package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.commands.SparklyClubesCommandExecutor
import net.perfectdreams.dreamclubes.tables.ClubeHomeUpgrades
import net.perfectdreams.dreamclubes.tables.ClubesHomes
import net.perfectdreams.dreamclubes.utils.ClubePermissionLevel
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.options.CommandOptions
import net.perfectdreams.dreamcore.utils.scheduler.onAsyncThread
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

class SetHomeClubeExecutor(m: DreamClubes) : SparklyClubesCommandExecutor(m) {
    inner class Options : CommandOptions() {
        val name = optionalGreedyString("name")
    }

    override val options = Options()

    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()
        val name = args[options.name]?.substringBefore(" ") ?: "clube"

        withPlayerClube(player) { clube, selfMember ->
            if (!selfMember.permissionLevel.canExecute(ClubePermissionLevel.ADMIN)) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem permissão para fazer isto!")
                return@withPlayerClube
            }

            val location = player.location
            val success = onAsyncThread {
                transaction(Databases.databaseNetwork) {
                    val clubeMaxSetHomeCount = ClubeHomeUpgrades.select { ClubeHomeUpgrades.clube eq clube.id }
                        .count() + 1

                    // Not equal because we don't care if the user is just changing their current clube home
                    val currentClubeSetHomeCount = ClubesHomes.select { ClubesHomes.clube eq clube.id and (ClubesHomes.name neq name) }
                            .count()

                    if (currentClubeSetHomeCount >= clubeMaxSetHomeCount)
                        return@transaction false

                    transaction(Databases.databaseNetwork) {
                        ClubesHomes.upsert(ClubesHomes.clube, ClubesHomes.name) {
                            it[ClubesHomes.clube] = clube.id
                            it[ClubesHomes.name] = name
                            it[ClubesHomes.x] = location.x
                            it[ClubesHomes.y] = location.y
                            it[ClubesHomes.z] = location.z
                            it[ClubesHomes.yaw] = location.yaw
                            it[ClubesHomes.pitch] = location.pitch
                            it[ClubesHomes.worldName] = location.world.name
                        }
                    }

                    return@transaction true
                }
            }

            if (!success) {
                player.sendMessage("${DreamClubes.PREFIX} §cVocê não tem mais slots de casas suficientes! Compre novas casas em §6/lojacash")
                return@withPlayerClube
            }

            player.sendMessage("${DreamClubes.PREFIX} §aCasa do clube marcada com sucesso! Nome: §e$name")
        }
    }
}