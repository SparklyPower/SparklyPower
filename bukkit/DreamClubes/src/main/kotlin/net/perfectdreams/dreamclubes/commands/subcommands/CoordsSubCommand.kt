package net.perfectdreams.dreamclubes.commands.subcommands

import net.perfectdreams.dreamclubes.DreamClubes
import net.perfectdreams.dreamclubes.dao.Clube
import net.perfectdreams.dreamclubes.dao.ClubeMember
import net.perfectdreams.dreamclubes.utils.ClubeAPI
import net.perfectdreams.dreamclubes.utils.async
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.extensions.centralize
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class CoordsSubCommand(val m: DreamClubes) : WithClubeSubCommand {
    override fun execute(player: Player, clube: Clube, selfMember: ClubeMember, args: Array<String>) {
        async {
            player.sendMessage("§8[ §bCoordenadas §8]".centralizeHeader())
            player.sendMessage("")
            val tg = TableGenerator(
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER,
                TableGenerator.Alignment.CENTER
            )

            tg.addRow("§7§oNome", "§7§oDist\u00e2ncia§r", "§7§oCoordenadas§r", "§7§oMundo§r")

            for (wrapper in clube.retrieveMembers()) {
                val pStr = Bukkit.getPlayer(wrapper.id.value)
                if (pStr != null) {
                    var dist = 0
                    var valid = true
                    try {
                        dist = Math.round(player.getLocation().distance(pStr.location)).toInt()
                    } catch (e: Exception) {
                        valid = false
                    }
                    val x = pStr.location.blockX
                    val y = pStr.location.blockY
                    val z = pStr.location.blockZ
                    tg.addRow(
                            pStr.name,
                            "§b" + if (valid) dist else "?",
                            "§3 $x, $y, $z",
                            " §f" + pStr.world.name

                    )
                }
            }
            for (line in tg.generate(TableGenerator.Receiver.CLIENT, true, true)) {
                player.sendMessage(line.centralize())
            }
            player.sendMessage("§f §3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-§3§m-§b§m-")
        }
    }
}