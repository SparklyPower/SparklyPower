package net.perfectdreams.dreamvanish.commands

import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamvanish.DreamVanish
import net.perfectdreams.dreamvanish.DreamVanishAPI

object VanishCommand : DSLCommandBase<DreamVanish> {
    override fun command(plugin: DreamVanish) = create(
        listOf("vanish")
    ) {
        permission = "dreamvanish.vanish"

        executes {
            if (DreamVanishAPI.isVanished(this.player)) {
                DreamVanishAPI.setVanishedStatus(this.player, false)
                player.sendMessage("§aVocê agora está §linvisível§a!")
            } else {
                DreamVanishAPI.setVanishedStatus(this.player, true)
                player.sendMessage("§aVocê agora está §lvísível§a!")
            }
        }
    }
}