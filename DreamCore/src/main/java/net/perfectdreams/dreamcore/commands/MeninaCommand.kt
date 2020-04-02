package net.perfectdreams.dreamcore.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.MeninaAPI
import net.perfectdreams.dreamcore.utils.commands.DSLCommandBase
import net.perfectdreams.dreamcore.utils.extensions.girl
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

object MeninaCommand : DSLCommandBase<DreamCore> {
    override fun command(plugin: DreamCore) = create(listOf("menina", "mulher", "garota", "girl")) {
        executes {
            val scheduler = Bukkit.getScheduler()
            if (player.girl) {
                player.sendMessage(MeninaAPI.PREFIX_GIRL + "§cVocê já é uma menina! Se você quiser voltar a ser um menino, use §6/menino")
            } else {
                scheduler.schedule(plugin, SynchronizationContext.ASYNC) {
                    player.girl = true

                    player.sendMessage(MeninaAPI.PREFIX_GIRL + "§aVocê virou uma menina! Se você quiser voltar a ser um menino, use §6/menino")
                }
            }
        }
    }
}