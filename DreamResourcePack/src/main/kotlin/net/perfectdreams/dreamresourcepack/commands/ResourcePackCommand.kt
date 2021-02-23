package net.perfectdreams.dreamresourcepack.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.bukkit.SparklyCommand
import net.perfectdreams.dreamresourcepack.DreamResourcePack
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.player.PlayerResourcePackStatusEvent

class ResourcePackCommand(val m: DreamResourcePack) : SparklyCommand(arrayOf("resourcepack")) {
    @Subcommand
    fun showList(sender: CommandSender) {
        sendResourcePack(sender)
    }

    @Subcommand(["enviar"])
    fun sendResourcePack(sender: CommandSender) {
        sender.sendMessage("§aPara aproveitar o SparklyPower ao máximo, recomendamos que você instale o nosso pacote de recursos, que adiciona novos itens, blocos, músicas e muito mais!")
        sender.sendMessage("")
        sender.sendMessage("§aVocê pode ativar a resource pack do servidor editando o SparklyPower na sua lista de servidores! Ao clicar em \"Editar\", terá uma opção para ativar o download automático da resource pack do servidor!")
        sender.sendMessage("")
        sender.sendMessage("§aQuando o pacote de recursos precisar de atualização, o Minecraft irá baixar automaticamente ela!")
        sender.sendMessage("")
        sender.sendMessage("§7Se você preferir, você pode baixar o pacote de texturas manualmente em §6/resourcepack link")
    }

    @Subcommand(["link"])
    fun viewResourcePackLink(sender: CommandSender) {
        sender.sendMessage("§aLink da Resource Pack: §3${m.config.getString("link")!!}")
        sender.sendMessage("")
        sender.sendMessage("§cLembrando que atualizamos a Resource Pack do servidor periodicamente, então você terá que baixar manualmente toda hora que atualizar a textura!")
    }
}