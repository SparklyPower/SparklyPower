package net.perfectdreams.dreamscoreboard.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamscoreboard.DreamScoreboard
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class GlowingCommand(val m: DreamScoreboard) : SparklyCommand(arrayOf("glowing", "glow"), permission = "dreamscoreboard.glowing") {
    @Subcommand
    fun glow(player: Player) {
        if (player.isGlowing) {
            player.isGlowing = false
            player.sendMessage("§aAgora você parou de brilhar... que triste, né?")
        } else {
            player.isGlowing = true
            player.sendMessage("§aAgora você está brilhando amigx! Tá divaaaaaa :3")
        }
    }

    @Subcommand(["cor"])
    fun coloredGlow(player: Player, colorNameArray: Array<String>) {
        if (!player.hasPermission("dreamscoreboard.coloredglow")) {
            player.sendMessage("§cVocê precisa ser VIP+ ou superior para poder alterar a cor do seu brilho...")
            return
        }

        if (colorNameArray.isEmpty()) {
            player.sendMessage("§aCores disponíveis para você usar: ${FancyColor.values().filter { if (it.onlyVipPlusPlus) player.hasPermission("group.vip++") else true } .joinToString("§a, ", transform = { it.color.toString() + it.localizedName })}")
            player.sendMessage("§aPara ativar uma cor, use §6/glowing cor NomeDaCor")
        } else {
            val name = colorNameArray.joinToString(" ").toLowerCase()
            val colorRetrieved = FancyColor.values().sortedBy { it.localizedName }.firstOrNull { it.localizedName.startsWith(name) }

            if (colorRetrieved == null) {
                player.sendMessage("§c${name} não é uma cor válida!")
            } else {
                if (colorRetrieved.onlyVipPlusPlus && !player.hasPermission("group.vip++")) {
                    player.sendMessage("§cVocê não pode usar esta cor!")
                    return
                }

                m.coloredGlow[player.uniqueId] = colorRetrieved.color

                player.sendMessage("§aA sua cor foi alterada para ${colorRetrieved.localizedName}! Tá mais diva que antes amigx!")
            }
        }
    }

    enum class FancyColor(
            val localizedName: String,
            val color: ChatColor,
            val onlyVipPlusPlus: Boolean
    ) {
        BLACK("preto", ChatColor.BLACK, true),
        DARK_BLUE("azul escuro", ChatColor.DARK_BLUE, true),
        DARK_GREEN("verde escuro", ChatColor.DARK_GREEN, false),
        DARK_AQUA("azul água escuro", ChatColor.DARK_AQUA, false),
        DARK_RED("vermelho escuro", ChatColor.DARK_RED, false),
        GOLD("dourado", ChatColor.GOLD, false),
        GRAY("cinza", ChatColor.GRAY, false),
        DARK_GRAY("cinza escuro", ChatColor.DARK_GRAY, true),
        BLUE("azul", ChatColor.BLUE, true),
        GREEN("verde", ChatColor.GREEN, true),
        AQUA("azul água claro", ChatColor.AQUA, false),
        RED("vermelho claro", ChatColor.RED, true),
        DARK_PURPLE("roxo escuro", ChatColor.DARK_PURPLE, true),
        LIGHT_PURPLE("roxo claro", ChatColor.LIGHT_PURPLE, false),
        YELLOW("amarelo", ChatColor.YELLOW, true),
        WHITE("branco", ChatColor.WHITE, false)
    }
}