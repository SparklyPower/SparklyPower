package net.perfectdreams.dreamrestarter.commands

import com.github.salomonbrys.kotson.jsonObject
import kotlinx.coroutines.delay
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.utils.scheduler.onMainThread
import net.perfectdreams.dreamrestarter.DreamRestarter
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class RestartCommand(val m: DreamRestarter) : SparklyCommand(arrayOf("dreamrestart"), permission = "dreamrestart.use") {
    @Subcommand
    fun restart(sender: CommandSender, args: Array<String>) {
        if (args.getOrNull(0) == "now") {
            sender.sendMessage("§aServidor reiniciando... See you soon ^-^")

            m.launchMainThread {
                m.storeCurrentPlayersAndSendServerDownNotification()

                // Wait 2.5s before *really* shutting down
                delay(2_500)

                Bukkit.shutdown()
            }
        }
    }
}