package net.perfectdreams.dreamfight.commands

import net.perfectdreams.dreamfight.DreamFight
import net.perfectdreams.dreamfight.utils.FightModifier
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import org.bukkit.entity.Player

object FightSetupCommand : DSLCommandBase<DreamFight> {
    override fun command(plugin: DreamFight) = create(listOf("fight setup")) {
        permission = "dreamfight.setup"

        executes {
            if (args.getOrNull(0) == "set" && args.getOrNull(1) != null) {
                if (sender is Player) {
                    val p: Player = sender as Player
                        when (args[1]) {
                        "lobby" -> plugin.fight!!.lobby = p.getLocation()
                        "pos1" -> plugin.fight!!.pos1 = p.getLocation()
                        "pos2" -> plugin.fight!!.pos2 = p.getLocation()
                        "exit" -> plugin.fight!!.exit = p.getLocation()
                        "save" -> {
                            plugin.userData["Fight.Lobby"] = plugin.fight.lobby
                            plugin.userData["Fight.Pos1"] = plugin.fight.pos1
                            plugin.userData["Fight.Pos2"] = plugin.fight.pos2
                            plugin.userData["Fight.Exit"] = plugin.fight.exit

                            plugin.userData.save(plugin.dataYaml)
                        }
                    }
                }
            }
            if (args.getOrNull(0) == "start") {
                plugin.eventoFight.preStart()
            }

            if (args.getOrNull(0) == "modifier" && args.getOrNull(1) != null) {
                plugin.fight!!.modifiers.add(
                    FightModifier.valueOf(
                        args[1]
                    )
                )
            }
        }
    }
}