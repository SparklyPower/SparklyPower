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

class MeninoCommand(val m: DreamCore) : SparklyCommand(arrayOf("menino", "homem", "garoto", "boy")) {
    @Subcommand
    fun root(player: Player) {
        val scheduler = Bukkit.getScheduler()
        if (!player.girl) {
            player.sendMessage(MeninaAPI.PREFIX_BOY + "§cVocê já é um menino! Se você quiser voltar a ser um menina, use §6/menina")
        } else {
            scheduler.schedule(m, SynchronizationContext.ASYNC) {
                player.girl = false

                player.sendMessage(MeninaAPI.PREFIX_BOY + "§aVocê virou um menino! Se você quiser voltar a ser uma menina, use §6/menina")
            }
        }
    }
}