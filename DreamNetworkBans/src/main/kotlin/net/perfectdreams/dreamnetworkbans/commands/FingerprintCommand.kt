package net.perfectdreams.dreamnetworkbans.commands

import net.md_5.bungee.api.CommandSender
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.PunishmentManager
import net.perfectdreams.dreamnetworkbans.dao.Fingerprint
import net.perfectdreams.dreamnetworkbans.tables.Fingerprints
import net.perfectdreams.dreamnetworkbans.utils.prettyBoolean
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class FingerprintCommand(val m: DreamNetworkBans) : SparklyBungeeCommand(arrayOf("fingerprint"), permission = "dreamnetworkbans.fingerprint") {

	@Subcommand
	fun checkBan(sender: CommandSender, playerName: String) {
		val punishedUniqueId = try { UUID.fromString(playerName) } catch (e: IllegalArgumentException) { PunishmentManager.getUniqueId(playerName) }
		val punishedPlayer = m.proxy.getPlayer(punishedUniqueId)

		sender.sendMessage("§eSobre §b$playerName§e...".toTextComponent())

		if (punishedPlayer != null) {
			sender.sendMessage("§ePing: ${punishedPlayer.ping}ms".toTextComponent())
			sender.sendMessage("§7(Se o ping do player está \"estranho\", talvez seja hack ou bot!)".toTextComponent())
		}

		transaction(Databases.databaseNetwork) {
			val fingerprintRow = Fingerprints.select { Fingerprints.player eq punishedUniqueId }
					.sortedByDescending { Fingerprints.createdAt }
					.firstOrNull()

			if (fingerprintRow == null) {
				sender.sendMessage("§cNenhuma fingerprint está guardada para §b$punishedUniqueId...".toTextComponent())
				sender.sendMessage("§7(Talvez seja hack ou bot! ...ou alguém beeem lagado!)".toTextComponent())
			} else {
				val fingerprint = Fingerprint.wrapRow(fingerprintRow)

				// Estamos fazendo isto dentro de uma transaction!!
				// É bom? Não... mas fazer o que né
				sender.sendMessage("§eUsa Forge? ${fingerprint.isForgeUser.prettyBoolean()}".toTextComponent())
				sender.sendMessage("§eModo de Chat: ${fingerprint.chatMode}".toTextComponent())
				sender.sendMessage("§eMão Principal: ${fingerprint.mainHand}".toTextComponent())
				sender.sendMessage("§eLinguagem: ${fingerprint.language}".toTextComponent())
				sender.sendMessage("§eView Distance: ${fingerprint.viewDistance}".toTextComponent())
				sender.sendMessage("§eVersão: ${fingerprint.version}".toTextComponent())
				sender.sendMessage("§eCapa Ativada? ${fingerprint.hasCape.prettyBoolean()}".toTextComponent())
				sender.sendMessage("§eChapéu Ativado? ${fingerprint.hasHat.prettyBoolean()}".toTextComponent())
				sender.sendMessage("§eJaqueta Ativada? ${fingerprint.hasJacket.prettyBoolean()}".toTextComponent())
				sender.sendMessage("§ePerna Esquerda Ativada? ${fingerprint.hasLeftPants.prettyBoolean()}".toTextComponent())
				sender.sendMessage("§ePerna Direita Ativada? ${fingerprint.hasRightPants.prettyBoolean()}".toTextComponent())
				sender.sendMessage("§eBraço Esquerdo Ativado? ${fingerprint.hasLeftSleeve.prettyBoolean()}".toTextComponent())
				sender.sendMessage("§eBraço Direito Ativado? ${fingerprint.hasRightSleeve.prettyBoolean()}".toTextComponent())
			}
		}
	}
}