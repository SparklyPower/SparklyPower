package net.perfectdreams.dreamnetworkbans.commands

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.perfectdreams.commands.ArgumentType
import net.perfectdreams.commands.annotation.InjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.network.DreamNetwork
import net.perfectdreams.dreamcorebungee.utils.DreamUtils
import net.perfectdreams.dreamcorebungee.utils.ParallaxEmbed
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import net.perfectdreams.dreamnetworkbans.utils.CustomArgument
import net.perfectdreams.dreamnetworkbans.utils.DateUtils
import net.perfectdreams.dreamnetworkbans.utils.InjectCustomArgument
import java.util.*

class KickCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("kick"), permission = "dreamnetworkbans.kick") {
	
	@Subcommand
    fun kick(sender: CommandSender, playerName: String, @InjectArgument(ArgumentType.ALL_ARGUMENTS) reason: String?) {
		val (punishedDisplayName, punishedUniqueId, player) = PunishmentManager.getPunishedInfoByString(playerName) ?: run {
			sender.sendMessage("§cEu sei que você tá correndo para expulsar aquele mlk meliante... mas eu não conheço ninguém chamado §b$playerName§c... respira um pouco... fica calmo e VEJA O NOME NOVAMENTE!".toTextComponent())
			return
		}

		if (player == null) {
			sender.sendMessage("§cNós conhecemos $playerName, mas ele não está online!".toTextComponent())
			return
		}

		val punisherDisplayName = PunishmentManager.getPunisherName(sender)

		var effectiveReason = reason ?: "Sem motivo definido"
		
		var silent = false
		if (effectiveReason.endsWith("-f")) {
			player.disconnect("Internal Exception: java.io.IOException: An existing connection was forcibly closed by the remote host".trimIndent().toTextComponent())

			sender.sendMessage("§a${player.name} (${player.uniqueId}) kickado com sucesso pelo motivo \"$effectiveReason\"".toTextComponent())
			return
		}
		if (effectiveReason.endsWith("-s")) {
			silent = true
			
			effectiveReason = effectiveReason.substring(0, (effectiveReason.length - "-s".length) - 1)
		}

		PunishmentManager.sendPunishmentToDiscord(
				silent,
				punishedDisplayName ?: "Nome desconhecido",
				punishedUniqueId!!,
				"Expulso",
				punisherDisplayName,
				effectiveReason,
				(sender as? ProxiedPlayer)?.server?.info?.name,
				null
		)

		player.disconnect("""
            §cVocê foi expulso do servidor!
            §cMotivo:

            §a$effectiveReason
            §cPor: ${sender.name}
			§7Não se preocupe, você poderá voltar a jogar simplesmente entrando novamente no servidor!
		""".trimIndent().toTextComponent())
		sender.sendMessage("§a${player.name} (${player.uniqueId}) kickado com sucesso pelo motivo \"$effectiveReason\"".toTextComponent())
	}
}