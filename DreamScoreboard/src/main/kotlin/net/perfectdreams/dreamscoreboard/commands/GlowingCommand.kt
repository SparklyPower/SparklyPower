package net.perfectdreams.dreamscoreboard.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamscoreboard.DreamScoreboard
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

object GlowingCommand : DSLCommandBase<DreamScoreboard> {
    override fun command(plugin: DreamScoreboard) = create(listOf("glow", "glowing")) {
        permission = "dreamscoreboard.glowing"

        executes {
            if (player.isGlowing) {
                player.isGlowing = false
                player.sendMessage("§aAgora você parou de brilhar... que triste, né?")
            } else {
                player.isGlowing = true
                player.sendMessage("§aAgora você está brilhando amigx! Tá divaaaaaa :3")
                player.sendMessage("§aVocê pode trocar a cor do brilho com §6/glow cor")
            }
        }
    }
}