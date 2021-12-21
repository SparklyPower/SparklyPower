package net.perfectdreams.dreamterrainadditions.commands

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.CommandException
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.extensions.isBetween
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import org.bukkit.Bukkit
import org.bukkit.Location

object BanirCommand : DSLCommandBase<DreamTerrainAdditions> {
    override fun command(plugin: DreamTerrainAdditions) = create(listOf("banir")) {
        executes {
            val playerName = args.getOrNull(0) ?: throw CommandException("§e/banir <player>")

            val claim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)

            if (playerName == player.name) {
                player.sendMessage("§cVocê não pode banir você mesmo do terreno, bobinh${player.artigo}!")
                return@executes
            }

            if (claim == null) {
                player.sendMessage("§cFique em cima do terreno que você deseja banir alguém!")
                return@executes
            }

            if (claim.ownerName == player.name || claim.allowGrantPermission(player) == null) {
                val claimAdditions = plugin.getOrCreateClaimAdditionsWithId(claim.id)

                if (claimAdditions.bannedPlayers.contains(playerName)) {
                    player.sendMessage("§b$playerName§c já está banido deste terreno!")
                    return@executes
                }

                val banned = Bukkit.getPlayerExact(playerName)

                if (banned != null) {
                    if (banned.name == claim.ownerName) {
                        player.sendMessage("§b$playerName§c é o dono do terreno! Não é possível banir ele!")
                        return@executes
                    }

                    val lesser = Location(banned.world, claim.lesserBoundaryCorner.x, 0.0, claim.lesserBoundaryCorner.z)
                    val greater = Location(banned.world, claim.greaterBoundaryCorner.x, 255.0, claim.greaterBoundaryCorner.z)

                    if (banned.location.isBetween(lesser, greater)) {
                        banned.teleport(DreamCore.dreamConfig.getSpawn())
                        banned.sendTitle("§f", "§cVocê foi banido do terreno", 20, 60, 20)
                    }
                }

                claimAdditions.bannedPlayers.add(playerName)
                player.sendMessage("§b$playerName§a foi banido do terreno!")
                player.sendMessage("§7Veja os players banidos do seu terreno usando §6/claimbanlist")
                plugin.save()
            } else {
                player.sendMessage(withoutPermission)
                return@executes
            }
        }
    }
}