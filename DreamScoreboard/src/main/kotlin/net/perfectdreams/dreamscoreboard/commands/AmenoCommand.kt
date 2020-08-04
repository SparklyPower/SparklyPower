package net.perfectdreams.dreamscoreboard.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.md_5.bungee.api.ChatColor
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamscoreboard.DreamScoreboard
import org.bukkit.Bukkit
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

object AmenoCommand : DSLCommandBase<DreamScoreboard> {
    override fun command(plugin: DreamScoreboard) = create(listOf("ameno")) {
        permission = "dreamscoreboard.ameno"

        executes {
            plugin.schedule(SynchronizationContext.ASYNC) {
                val image = ImageIO.read(File(plugin.dataFolder, args[0]))

                for (y in 0 until image.height) {
                    val strBuilder = StringBuilder()

                    for (x in 0 until image.width) {
                        strBuilder.append("${ChatColor.of(Color(image.getRGB(x, y)))}⬛")
                    }

                    switchContext(SynchronizationContext.SYNC)

                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage(strBuilder.toString())
                    }

                    switchContext(SynchronizationContext.ASYNC)
                }
            }
        }
    }
}