package net.perfectdreams.dreamterrainadditions.commands

import me.ryanhamshire.GriefPrevention.ClaimPermission
import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.CommandException
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.isBetween
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawn
import net.perfectdreams.dreamcore.utils.extensions.teleportToServerSpawnWithEffects
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.Bukkit
import org.bukkit.Location

object RetirarCommand : DSLCommandBase<DreamTerrainAdditions> {
    override fun command(plugin: DreamTerrainAdditions) = create(listOf("retirar", "expulsar", "kickar", "tchau", "adeus")) {
        executes {
            val playerName = args.getOrNull(0) ?: throw CommandException("§e/retirar <player>")

            if (playerName == player.name) {
                player.sendMessage("§cVocê não pode remover você mesmo do terreno, bobinh${player.artigo}!")
                return@executes
            }

            val kick = Bukkit.getPlayer(playerName)

            if (kick == null || DreamVanishAPI.isQueroTrabalhar(kick)) {
                player.sendMessage("§b$playerName§c não existe ou está offline!")
                return@executes
            }

            if (kick.hasPermission("sparklypower.soustaff")) {
                player.sendMessage("§cVocê não pode expulsar alguém da Staff do seu terreno! Está tentando esconder o quê?")
                return@executes
            }

            val claim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)

            if (claim == null) {
                player.sendMessage("§cFique em cima do terreno que você deseja expulsar alguém!")
                return@executes
            }

            if (claim.ownerName == player.name || claim.checkPermission(player, ClaimPermission.Manage, null) == null) {
                // The Y must be expanded to the bottom and to the top of the world
                val lesser = Location(player.world, claim.lesserBoundaryCorner.x, player.world.minHeight.toDouble(), claim.lesserBoundaryCorner.z)
                val greater = Location(player.world, claim.greaterBoundaryCorner.x, (player.world.maxHeight - 1).toDouble(), claim.greaterBoundaryCorner.z)

                if (!kick.location.isBetween(lesser, greater)) {
                    player.sendMessage("§b${kick.displayName}§c não está neste terreno!")
                    return@executes
                }

                kick.teleportToServerSpawnWithEffects()
                kick.sendTitle("§f", "§cVocê foi expulso do terreno", 20, 60, 20)

                player.sendMessage("§b${kick.displayName}§a foi expulso do terreno!")
                player.sendMessage("§7Você pode banir alguém do seu terreno usando §6/banir")
            } else {
                player.sendMessage(withoutPermission)
                return@executes
            }
        }
    }
}