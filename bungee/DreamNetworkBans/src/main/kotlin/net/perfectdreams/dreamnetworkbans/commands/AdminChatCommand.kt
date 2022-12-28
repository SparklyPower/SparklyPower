package net.perfectdreams.dreamnetworkbans.commands

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.github.salomonbrys.kotson.jsonObject
import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.dreamcorebungee.commands.SparklyBungeeCommand
import net.perfectdreams.dreamcorebungee.dao.User
import net.perfectdreams.dreamcorebungee.network.DreamNetwork
import net.perfectdreams.dreamcorebungee.network.socket.SocketUtils
import net.perfectdreams.dreamcorebungee.utils.Constants
import net.perfectdreams.dreamcorebungee.utils.Databases
import net.perfectdreams.dreamcorebungee.utils.extensions.toBaseComponent
import net.perfectdreams.dreamcorebungee.utils.extensions.toTextComponent
import net.perfectdreams.dreamnetworkbans.DreamNetworkBans
import net.perfectdreams.dreamnetworkbans.utils.StaffColors
import net.perfectdreams.dreamnetworkbans.utils.emotes
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class AdminChatCommand : SparklyBungeeCommand(arrayOf("adminchat", "a"), permission = "dreamnetworkbans.adminchat") {
	companion object {
		private val bungee = BungeeCord.getInstance()
		val lockedChat = mutableSetOf<ProxiedPlayer>()
		private val adminChatColor = ChatColor.AQUA

		fun broadcastMessage(sender: CommandSender, text: String) {
			val staff = bungee.players.filter { it.hasPermission("dreamnetworkbans.adminchat") }

			val message = (sender as? ProxiedPlayer)?.let { player ->
				// The last color is a fallback, it checks for "group.default", so everyone should, hopefully, have that permission
				val role = StaffColors.values().first { player.hasPermission(it.permission) }

				val isGirl = transaction(Databases.databaseNetwork) {
					User.findById(player.uniqueId)?.isGirl ?: false
				}

				val colors = role.colors
				val prefix = with (role.prefixes) { if (isGirl && size == 2) get(1) else get(0) }
				val emote = emotes[player.name] ?: ""

				// Using different colors for each staff group is bad, because it is harder to track admin chat messages since all groups have different colors
				var colorizedText = " $text"

				staff.forEach {
					val regex = Regex(".*\\b${it.name}\\b.*")
					if (!text.matches(regex)) return@forEach

					DreamNetwork.PERFECTDREAMS_SURVIVAL.sendAsync(
						jsonObject(
							"type" to "staffMention",
							"target" to it.name,
							"player" to player.name,
							"highlight" to colors.chat.toString(),
							"textColor" to colors.nick.toString()
						)
					)

					colorizedText = colorizedText.replace(Regex("\\b${it.name}\\b"), colors.mention(it.name))
				}

				"$prefix $emote $adminChatColor${player.name}:$colorizedText".toTextComponent().apply {
					hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "§3Servidor: §b${player.server.info.name}".toBaseComponent())
				}
			} ?: "\ue252 §x§a§8§a§8§a§8Mensagem do console: §x§c§6§b§f§c§3$text".toTextComponent()

			staff.forEach { it.sendMessage(message) }

			DreamNetworkBans.INSTANCE.adminChatWebhook.send(
				WebhookMessageBuilder()
					.setUsername(sender.name)
					.setAvatarUrl("https://sparklypower.net/api/v1/render/avatar?name=${sender.name}&scale=16")
					.setContent(text)
					.build()
			)
		}
	}

	@Subcommand
	fun adminChat(sender: CommandSender, args: Array<String>) =
		with (args) {
			when {
				isEmpty() -> return sender.sendMessage("§cVocê não pode enviar uma mensagem vazia.")

				getOrNull(0) == "bypass" -> {
					DreamNetworkBans.bypassPremiumCheck = !DreamNetworkBans.bypassPremiumCheck
					return sender.sendMessage("Bypass Status: ${DreamNetworkBans.bypassPremiumCheck}")
				}

				getOrNull(0) == "lock" -> {
					val player = sender as? ProxiedPlayer ?: return sender.sendMessage("§cSó jogadores conseguem trancar o chat.")

					with (lockedChat) {
						val isLocked = player in this
						if (isLocked) remove(player) else add(player)

						player.sendMessage("§x§8§3§9§E§F§7Seu chat foi ${if (isLocked) "des" else ""}travado com sucesso.")
					}
				}

				else -> broadcastMessage(sender, args.joinToString(" "))
			}
		}
}