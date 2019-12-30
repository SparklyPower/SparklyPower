package net.perfectdreams.dreamcore.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.MeninaAPI
import net.perfectdreams.dreamcore.utils.extensions.girl
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class MeninaCommand(val m: DreamCore) : SparklyCommand(arrayOf("menina", "mulher", "garota", "girl")) {
    @Subcommand
    fun root(player: Player) {
        val scheduler = Bukkit.getScheduler()
        if (player.girl) {
            player.sendMessage(MeninaAPI.PREFIX_GIRL + "§cVocê já é uma menina! Se você quiser voltar a ser um menino, use §6/menino")
        } else {
            scheduler.schedule(m, SynchronizationContext.ASYNC) {
                player.girl = true

                player.sendMessage(MeninaAPI.PREFIX_GIRL + "§aVocê virou uma menina! Se você quiser voltar a ser um menino, use §6/menino")
            }
        }
    }
}