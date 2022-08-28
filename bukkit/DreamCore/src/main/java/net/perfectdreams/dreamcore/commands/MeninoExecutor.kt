package net.perfectdreams.dreamcore.commands

import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.MeninaAPI
import net.perfectdreams.dreamcore.utils.commands.context.CommandArguments
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.executors.SparklyCommandExecutor
import net.perfectdreams.dreamcore.utils.extensions.girl
import org.bukkit.Bukkit

class MeninoExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

        val scheduler = Bukkit.getScheduler()
        if (!player.girl) {
            player.sendMessage(MeninaAPI.PREFIX_BOY + "§cVocê já é um menino! Se você quiser voltar a ser um menina, use §6/menina")
        } else {
            scheduler.schedule(plugin, SynchronizationContext.ASYNC) {
                player.girl = false

                player.sendMessage(MeninaAPI.PREFIX_BOY + "§aVocê virou um menino! Se você quiser voltar a ser uma menina, use §6/menina")
            }
        }
    }
}