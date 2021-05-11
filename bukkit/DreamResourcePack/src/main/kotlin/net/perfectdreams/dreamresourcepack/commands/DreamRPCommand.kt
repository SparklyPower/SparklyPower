package net.perfectdreams.dreamresourcepack.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamresourcepack.DreamResourcePack
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.player.PlayerResourcePackStatusEvent

class DreamRPCommand(val m: DreamResourcePack) : SparklyCommand(arrayOf("dreamrp"), permission = "dreamrp.use") {
    @Subcommand
    fun showList(sender: CommandSender) {
        Bukkit.getOnlinePlayers().forEach {
            sender.sendMessage("§b${it.name}§8: ${statusColor(it.resourcePackStatus)}${it.resourcePackStatus ?: "???"}")
        }
    }

    @Subcommand(["reload"])
    fun reload(sender: CommandSender) {
        m.reloadConfig()
        sender.sendMessage("§aRecarregado com sucesso!")
    }

    private fun statusColor(status: PlayerResourcePackStatusEvent.Status?): String {
        return when (status) {
            PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED -> "§a"
            PlayerResourcePackStatusEvent.Status.DECLINED -> "§4"
            PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD -> "§c"
            PlayerResourcePackStatusEvent.Status.ACCEPTED -> "§e"
            else -> "§7"
        }
    }

    @Subcommand(["resend"])
    fun resendPack(sender: CommandSender, player: String) {
        val onlinePlayer = Bukkit.getPlayer(player)!!

        onlinePlayer.setResourcePack(
            m.config.getString("link")!!,
            m.config.getString("hash")!!
        )

        sender.sendMessage("Resource Pack reenviada para $onlinePlayer!")
    }

    @Subcommand(["resend"])
    fun resendPack(sender: CommandSender, player: String, link: String, hash: String) {
        val onlinePlayer = Bukkit.getPlayer(player)!!

        onlinePlayer.setResourcePack(
            link,
            hash
        )

        sender.sendMessage("Resource Pack $link $hash reenviada para $onlinePlayer!")
    }

    @Subcommand(["resend_all"])
    fun resendPack(sender: CommandSender) {
        Bukkit.getOnlinePlayers().forEach {
            it.setResourcePack(
                m.config.getString("link")!!,
                m.config.getString("hash")!!
            )
        }
        sender.sendMessage("Resource Pack reenviada para todos os players!")
    }

    @Subcommand(["resend_all"])
    fun resendPack(sender: CommandSender, link: String, hash: String) {
        Bukkit.getOnlinePlayers().forEach {
            it.setResourcePack(
                link,
                hash
            )
        }
        sender.sendMessage("Resource Pack $link $hash reenviada para todos os players!")
    }
}