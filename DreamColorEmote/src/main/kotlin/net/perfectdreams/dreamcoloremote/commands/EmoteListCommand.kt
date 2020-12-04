package net.perfectdreams.dreamcoloremote.commands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.perfectdreams.dreamcoloremote.DreamColorEmote
import net.perfectdreams.dreamcore.utils.commands.CommandException
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.plusAssign
import net.perfectdreams.dreamcore.utils.toBaseComponent
import net.perfectdreams.dreamcore.utils.translateColorCodes
import org.bukkit.ChatColor

object EmoteListCommand : DSLCommandBase<DreamColorEmote> {
    override fun command(plugin: DreamColorEmote) = create(
        listOf("emote", "emoji")
    ) {
        executes {
            val page = args.getOrNull(0)?.toIntOrNull() ?: 0
            val paginaQuantidade = page*20;

            val emoteList = mutableListOf<Int>()

            emoteList.addAll(listOf(38146,38147,38148,38149,38150,38151,38152,38153,38154,38155,38156,38157,38158,38159,38223,38237,38238,38239,38270,38271,38280,38281,38282,38283,38284,38285,38311,38312,38313,38314,38315,38316,38317,38318,38319))
            emoteList.addAll((23040..23295).toList())

            if (page > (emoteList.size/20).toInt() || page < 0) {
                throw CommandException("${ChatColor.RED}/emoji 0-${(emoteList.size/20).toInt()}")
            }

            val textComponent = TextComponent()

            for (i in 0..19) {
               if ((paginaQuantidade+i)+1 <= emoteList.size) {
				   val emoji = if (i == 4 || i == 9 || i == 14) "${ChatColor.DARK_AQUA}[${ChatColor.RESET}"+emoteList[paginaQuantidade+i].toChar().toString()+"${ChatColor.DARK_AQUA}]\n" 
				   else "${ChatColor.DARK_AQUA}[${ChatColor.RESET}"+emoteList[paginaQuantidade+i].toChar().toString()+"${ChatColor.DARK_AQUA}] "
				   	 
                   textComponent += TextComponent(emoji.translateColorCodes()).apply {
                       clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, emoteList[paginaQuantidade+i].toChar().toString())
                       hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "Clique para colar ${emoteList[paginaQuantidade+i].toChar()} no chat".toBaseComponent())
                   }
               }
            }

            val paginaDetalhe = if (page+1 > (emoteList.size/20).toInt()) "${ChatColor.DARK_AQUA}Página ${ChatColor.YELLOW}${page}${ChatColor.DARK_AQUA}/${ChatColor.YELLOW}${(emoteList.size/20).toInt()}"
            else "${ChatColor.DARK_AQUA}Página ${ChatColor.YELLOW}${page}${ChatColor.DARK_AQUA}/${ChatColor.YELLOW}${(emoteList.size/20).toInt()}${ChatColor.GREEN} - Vá para a próxima página usando ${ChatColor.YELLOW}/emoji ${page+1}"

			sender.sendMessage("")
			sender.sendMessage("${ChatColor.GREEN}Clique em algum emoji ${ChatColor.RESET}婴娮")
			sender.sendMessage("")
            sender.sendMessage(textComponent)
			sender.sendMessage("")
            sender.sendMessage(paginaDetalhe)
        }
    }
}
