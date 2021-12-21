package net.perfectdreams.dreamterrainadditions.commands

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.perfectdreams.dreamcore.utils.commands.CommandException
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.artigo
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions

object DesbanirCommand : DSLCommandBase<DreamTerrainAdditions> {
    override fun command(plugin: DreamTerrainAdditions) = create(listOf("desbanir")) {
        executes {
            val playerName = args.getOrNull(0) ?: throw CommandException("§e/desbanir <player>")

            val claim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)

            if (playerName == player.name) {
                player.sendMessage("§cVocê não pode desbanir você mesmo do terreno, bobinh${player.artigo}!")
                return@executes
            }

            if (claim == null) {
                player.sendMessage("§cFique em cima do terreno que você deseja desbanir alguém!")
                return@executes
            }

            if (claim.ownerName == player.name || claim.managers.contains(player.name)) {
                val claimAdditions = plugin.getOrCreateClaimAdditionsWithId(claim.id)

                if (!claimAdditions.bannedPlayers.contains(playerName)) {
                    player.sendMessage("§b$playerName§c não está banido deste terreno!")
                    return@executes
                }

                claimAdditions.bannedPlayers.remove(playerName)
                player.sendMessage("§b$playerName§a foi desbanido do terreno!")
                player.sendMessage("§7Para banir alguém, use §6/banir")
                plugin.save()
            } else {
                player.sendMessage(withoutPermission)
                return@executes
            }
        }
    }
}