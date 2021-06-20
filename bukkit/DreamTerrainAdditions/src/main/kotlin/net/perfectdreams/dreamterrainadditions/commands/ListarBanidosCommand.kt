package net.perfectdreams.dreamterrainadditions.commands

import me.ryanhamshire.GriefPrevention.GriefPrevention
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.perfectdreams.dreamcore.utils.commands.Command
import net.perfectdreams.dreamcore.utils.commands.CommandContext
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.plusAssign
import net.perfectdreams.dreamcore.utils.toBaseComponent
import net.perfectdreams.dreamcore.utils.translateColorCodes
import net.perfectdreams.dreamcore.utils.withoutPermission
import net.perfectdreams.dreamterrainadditions.DreamTerrainAdditions
import org.bukkit.ChatColor

object  ListarBanidosCommand : DSLCommandBase<DreamTerrainAdditions> {
    override fun command(plugin: DreamTerrainAdditions) = create(listOf("claimbanlist")) {
        executes {
            val claim = GriefPrevention.instance.dataStore.getClaimAt(player.location, false, null)

            if (claim == null) {
                player.sendMessage("§c§lFique dentro do seu terreno")
                return@executes
            }

            if (claim.ownerName == player.name || claim.allowGrantPermission(player) == null) {
                val claimAdditions = plugin.getClaimAdditionsById(claim.id) ?: return@executes

                val textComponent = TextComponent()

                for ((i, v) in claimAdditions.bannedPlayers.withIndex()) {
                    var playerName = v

                    if ((claimAdditions.bannedPlayers.size - 1) != i)
                        playerName += "\n"

                    textComponent += TextComponent("${ChatColor.RED}${ChatColor.BOLD}$playerName".translateColorCodes().centralize()).apply {
                        clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/desbanir $v")
                        hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "Clique para desbanir $v do seu terreno".toBaseComponent())
                    }
                }

                player.sendMessage("§cPlayers banidos deste claim".centralizeHeader())
                player.sendMessage("")
                if (claimAdditions.bannedPlayers.size > 0) {
                    player.sendMessage(textComponent)
                    player.sendMessage("\n§3Clique no nome do player banido para desbanir ele.")
                } else {
                    player.sendMessage("§6Nenhum player está banido do seu terreno!".centralize())
                }

                player.sendMessage("§c-".centralizeHeader())

            } else {
                player.sendMessage(withoutPermission)
                return@executes
            }
        }
    }
}