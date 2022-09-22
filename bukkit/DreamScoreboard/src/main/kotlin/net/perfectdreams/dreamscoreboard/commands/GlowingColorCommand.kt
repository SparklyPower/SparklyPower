package net.perfectdreams.dreamscoreboard.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.centralizeHeader
import net.perfectdreams.dreamcore.utils.set
import net.perfectdreams.dreamscoreboard.DreamScoreboard
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.concurrent.TimeUnit

object GlowingColorCommand : DSLCommandBase<DreamScoreboard> {
    override fun command(plugin: DreamScoreboard) = create(listOf("glow cor", "glowing cor")) {
        permission = "dreamscoreboard.coloredglow"

        executes {
            val colorNameArray = args

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
                        return@executes
                    }

                    player.persistentDataContainer.set(DreamScoreboard.GLOW_COLOR_KEY, colorRetrieved.color.name)

                    player.sendMessage("§aA sua cor foi alterada para ${colorRetrieved.localizedName}! Tá mais diva que antes amigx!")
                }
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