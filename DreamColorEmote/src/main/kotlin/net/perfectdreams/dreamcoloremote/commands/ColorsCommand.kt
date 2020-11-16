package net.perfectdreams.dreamcoloremote.commands

import net.perfectdreams.dreamcoloremote.DreamColorEmote
import net.perfectdreams.dreamcore.utils.TableGenerator
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import org.bukkit.ChatColor
import java.awt.Color

object ColorsCommand : DSLCommandBase<DreamColorEmote> {
    override fun command(plugin: DreamColorEmote) = create(
        listOf("mccores")
    ){
        executes {
            val subCommand = args.getOrNull(0)

            if (subCommand == "rgb") {
                val r = args.getOrNull(1)?.replace(",", "")?.trim()?.toInt()
                val g = args.getOrNull(2)?.replace(",", "")?.trim()?.toInt()
                val b = args.getOrNull(3)?.replace(",", "")?.trim()?.toInt()

                if (r == null || g == null || b == null) {
                    sender.sendMessage("§cInsira uma cor no formato RGB para que ela possa ser traduzida para ser usada no minecraft.")
                    return@executes
                }

                if (r !in 0..255 || g !in 0..255 || b !in 0..255) {
                    sender.sendMessage("§cCor inválida! §e/mccores rgb <§30§e-§c255§e>, <§30§e-§c255§e>, <§30§e-§c255§e>")
                    return@executes
                }

                val color = Color(r, g, b)
                val hex = String.format("%02x%02x%02x", color.red, color.green, color.blue)

                val strBuilder = buildString {
                    this.append("&x")
                    hex.forEach {
                        this.append("&")
                        this.append(it)
                    }
                }

                sender.sendMessage("§3Formato de cor para o chat: §r${strBuilder.replace("&", "§")}$strBuilder")
				return@executes
            }

            val tg = TableGenerator(
                    TableGenerator.Alignment.LEFT,
                    TableGenerator.Alignment.LEFT
            )

            tg.addRow("${ChatColor.BOLD}&0 = ${ChatColor.BLACK}Preto", "${ChatColor.RESET}${ChatColor.BOLD}&1 = ${ChatColor.DARK_BLUE}Azul Escuro")
            tg.addRow("${ChatColor.BOLD}&2 = ${ChatColor.DARK_GREEN}Verde Escuro","${ChatColor.RESET}${ChatColor.BOLD}&3 = ${ChatColor.DARK_AQUA}Ciano Escuro")
            tg.addRow("${ChatColor.BOLD}&4 = ${ChatColor.DARK_RED}Vermelho Escuro","${ChatColor.RESET}${ChatColor.BOLD}&5 = ${ChatColor.DARK_PURPLE}Rosa Escuro")
            tg.addRow("${ChatColor.BOLD}&6 = ${ChatColor.GOLD}Dourado","${ChatColor.RESET}${ChatColor.BOLD}&7 = ${ChatColor.GRAY}Cinza")
            tg.addRow("${ChatColor.BOLD}&8 = ${ChatColor.DARK_GRAY}Cinza Escuro","${ChatColor.RESET}${ChatColor.BOLD}&9 = ${ChatColor.BLUE}Azul")
            tg.addRow("${ChatColor.BOLD}&a = ${ChatColor.GREEN}Verde","${ChatColor.RESET}${ChatColor.BOLD}&b = ${ChatColor.AQUA}Ciano")
            tg.addRow("${ChatColor.BOLD}&c = ${ChatColor.RED}Vermelho","${ChatColor.RESET}${ChatColor.BOLD}&d = ${ChatColor.LIGHT_PURPLE}Rosa Claro")
            tg.addRow("${ChatColor.BOLD}&e = ${ChatColor.YELLOW}Amarelo","${ChatColor.RESET}${ChatColor.BOLD}&f = ${ChatColor.WHITE}Branco")
            tg.addRow("${ChatColor.BOLD}&k = ${ChatColor.MAGIC}Mágica${ChatColor.RESET}","${ChatColor.BOLD}&n = ${ChatColor.BOLD}Negrito")
            tg.addRow("${ChatColor.BOLD}&m = ${ChatColor.STRIKETHROUGH}Traçado${ChatColor.RESET}","${ChatColor.BOLD}&r = ${ChatColor.UNDERLINE}Sublinhado")
            tg.addRow("${ChatColor.BOLD}&o = ${ChatColor.ITALIC}Italico${ChatColor.RESET}","${ChatColor.BOLD}&5 = ${ChatColor.RESET}Resetar")

            sender.sendMessage("${ChatColor.BOLD}${ChatColor.GOLD}========== ${ChatColor.RED}Chat Cores & Estilos ${ChatColor.GOLD}==========")
            for (line in tg.generate(TableGenerator.Receiver.CLIENT, false, true)) {
                sender.sendMessage(line)
            }
            sender.sendMessage("${ChatColor.BOLD}${ChatColor.GOLD}${"=".repeat(38)}")
			sender.sendMessage("")	
			sender.sendMessage("§3Para tons mais específicos de cores, use §e/mccores rgb")
        }
    }
}
