package net.perfectdreams.dreamfight.commands

import net.perfectdreams.dreamfight.DreamFight
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase

object FightCommand : DSLCommandBase<DreamFight> {
    override fun command(plugin: DreamFight) = create(listOf("fight")) {
        permission = "dreamfight.joinevent"

        executes {
            if (plugin.fight.preStart) {
                plugin.fight.addToFight(player)
            } else {
                if (plugin.fight.started) {
                    if (args.getOrNull(0) == "camarote") {
                        player.sendMessage(DreamFight.prefix + "§aVocê foi até o §lcamarote§a, divirta-se!")
                        player.teleport(plugin.fight.exit)
                    } else {
                        player.sendMessage(DreamFight.prefix + "§cO Evento Fight §ljá começou§c... Se você quiser ver o Evento, use §e/fight camarote§c.")
                    }
                } else {
                    player.sendMessage(DreamFight.prefix + "§cAtualmente §lnão está acontecendo nenhum§c Evento Fight...")
                }
            }
        }
    }
}