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

class MeninaExecutor(val plugin: DreamCore) : SparklyCommandExecutor() {
    
    override fun execute(context: CommandContext, args: CommandArguments) {
        val player = context.requirePlayer()

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