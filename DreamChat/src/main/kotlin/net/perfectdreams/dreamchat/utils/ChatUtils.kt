package net.perfectdreams.dreamchat.utils

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.okkero.skedule.SynchronizationContext
import com.okkero.skedule.schedule
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamcore.network.DreamNetwork
import net.perfectdreams.dreamcore.network.socket.SocketUtils
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamvanish.DreamVanishAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import java.util.*

object ChatUtils {
	fun beautifyMessage(sender: Player, str: String): String {
		var message = str

		for (player in Bukkit.getOnlinePlayers().filterNot { DreamVanishAPI.isQueroTrabalhar(it) }) {
			val regex = Regex(".*\\b${player.name}\\b.*")
			if (message.matches(regex)) {
				message = message.replace(Regex("\\b${player.name}\\b", RegexOption.IGNORE_CASE), "§3${player.displayName}§f")
				player.playSound(player.location, "perfectdreams.sfx.msn", 1F, 1F)
				player.sendActionBar("§3${sender.displayName}§a te mencionou no chat!")
			}
		}

		DreamChat.INSTANCE.replacers.forEach {
			message = message.replace(it.key, it.value)
		}

		if (!message[0].isUpperCase() && !message.startsWith("http")) {
			message = message[0].toUpperCase() + message.substring(1)
		}

		if (message.contains("#")) {
			message = Regex("#\\w+").replace(message, "§9$0§f")
		}

		if (message.contains("/")) {
			message = Regex("\b/\\w+\b").replace(message, "§6$0§f")
		}

		return message
	}

	fun sendResponseAsBot(player: Player, message: String) {
		// Hora de "montar" a mensagem
		val textComponent = TextComponent()

		textComponent += TextComponent(*DreamChat.BOT_PREFIX.toBaseComponent()).apply {
			hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "kk eae men".toBaseComponent())
		}

		textComponent += TextComponent(*"§7${DreamChat.BOT_NAME}".translateColorCodes().toBaseComponent()).apply {
			hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
				"""§6✪ §a§lSobre a §r${DreamChat.BOT_NAME} §6✪
						|
						|§eGênero: §d♀
						|§eGrana: §6Incontáveis Sonhos
						|§eKDR: §6PvP é para os fracos, 2bj :3
						|§eOnline no SparklyPower Survival por §6mais tempo que você
					""".trimMargin().toBaseComponent())
			clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "Pantufa... só para saber, porque você quer me marcar? Eu sou um robô e não um player!")
		}
		textComponent += " §6➤ ".toBaseComponent()

		// Para não encher o chat de mensagens da Pantufa, vamos fazer que só apareça o texto ao passar o mouse em cima da mensagem dela
		val forEveryone = textComponent.duplicate()
		val lore = mutableListOf<String>()
		val currentLoreLine = StringBuilder()
		var currentTextSize = 0

		val split = message.split("(?=\\b[ ])")
		var previous: String? = null

		for (piece in split) {
			if (currentTextSize >= 40) {
				lore += currentLoreLine.toString()
				currentTextSize = 0
			}

			var editedPiece = piece
			if (previous != null) {
				editedPiece = "$previous$editedPiece"
			}
			textComponent += editedPiece.toBaseComponent()
			currentLoreLine.append(piece)
			currentTextSize += piece.length

			previous = ChatColor.getLastColors(piece)
		}
		lore += currentLoreLine.toString()

		forEveryone.addExtra(
			"§6[Passe o Mouse em Cima]".toTextComponent()
				.apply {
					hoverEvent = HoverEvent(
						HoverEvent.Action.SHOW_TEXT,
						lore.joinToString("\n").toBaseComponent()
					)
				}
		)

		Bukkit.getOnlinePlayers().forEach {
			if (it != player)
				it.sendMessage(forEveryone)
			else
				it.sendMessage(textComponent)
		}
	}

	fun sendResponseAsLoritta(player: Player, message: String) {
		// Hora de "montar" a mensagem
		val textComponent = TextComponent()

		textComponent += TextComponent(*DreamChat.LORITTA_PREFIX.toBaseComponent()).apply {
			hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, "kk eae men".toBaseComponent())
		}

		textComponent += TextComponent(*"§7${DreamChat.LORITTA_NAME}".translateColorCodes().toBaseComponent()).apply {
			hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT,
				"""§6✪ §a§lSobre a §r${DreamChat.LORITTA_NAME} §6✪
						|
						|§eGênero: §d♀
						|§eGrana: §6Incontáveis Sonhos
						|§eKDR: §6PvP é para os fracos, 2bj :3
						|§eOnline no SparklyPower Survival por §6mais tempo que você
						|
						|§dA mascote do SparklyPower e o bot mais fofis para o Discord!
					""".trimMargin().toBaseComponent())
			clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "Loritta... só para saber, porque você quer me marcar? Eu sou um robô e não um player!")
		}
		textComponent += " §6➤ ".toBaseComponent()

		val split = message.split("(?=\\b[ ])")
		var previous: String? = null
		for (piece in split) {
			var editedPiece = piece
			if (previous != null) {
				editedPiece = "$previous$editedPiece"
			}
			textComponent += editedPiece.toBaseComponent()
			previous = ChatColor.getLastColors(piece)
		}

		broadcast(textComponent)
	}

	fun isMensagemPolemica(message: String): Boolean {
		val message = message.toLowerCase()

		return message.matches(Regex(".*(cheat).*")) || message.matches(Regex(".*(x( |-)?ray).*")) || message.matches(Regex(".*(kill( )?aura).*")) || message.matches(Regex(".*(auto( )click).*")) || message.matches(Regex(".*(force( )field).*")) || message.matches(Regex(".*(anti( )(knock|kb)).*")) || message.matches(Regex(".*(raio( |-)?x).*")) || message.matches(Regex(".*(hack|hazuni|huzuni|nodus|weep( |-|_)?craft|flare|resilience|kryptonite|wurst|reflex).*")) || message.matches(Regex(".*(dup)")) || message.matches(Regex(".*(bug)")) || message.matches(Regex(".*(bugado).*"))
	}

	fun sendTell(sender: Player, receiver: Player, message: String) {
		val fromCanBeSeen = sender.displayName.stripColors()!!.contains(sender.name)
		val toCanBeSeen = receiver.displayName.stripColors()!!.contains(receiver.name)
		val isIgnoringTheSender = DreamChat.INSTANCE.userData.getStringList("ignore.${receiver.uniqueId}").contains(sender.uniqueId.toString())

		val fromName = if (!fromCanBeSeen) {
			sender.displayName + "§d (${sender.name})"
		} else {
			sender.displayName
		}

		val toName = if (!toCanBeSeen) {
			receiver.displayName + "§d (${receiver.name})"
		} else {
			receiver.displayName
		}

		if (!isIgnoringTheSender)
			receiver.sendMessage("§dDe §b${fromName}§r§d: §d$message")
		sender.sendMessage("§dPara §b${toName}§r§d: §d$message")

		DreamChat.INSTANCE.quickReply[receiver] = sender

		scheduler().schedule(DreamChat.INSTANCE, SynchronizationContext.ASYNC) {
			val calendar = Calendar.getInstance()
			val date = "${String.format("%02d", calendar[Calendar.DAY_OF_MONTH])}/${String.format("%02d", calendar[Calendar.MONTH] + 1)}/${String.format("%02d", calendar[Calendar.YEAR])} ${String.format("%02d", calendar[Calendar.HOUR_OF_DAY])}:${String.format("%02d", calendar[Calendar.MINUTE])}"
			DreamChat.INSTANCE.pmLog.appendText("[$date] ${sender.name} -> ${receiver.name}: $message\n")

			val json = JsonObject()
			json["type"] = "sendMessage"
			json["message"] = "`[$date]` \uD83D\uDD75 **`${sender.name}`** » **`${receiver.name}`**: $message\n"
			json["textChannelId"] = "378319231651151892"

			SocketUtils.send(json, port = 60799)
		}

		for (staff in Bukkit.getOnlinePlayers().filter { it.hasPermission("dreamchat.snoop") }) {
			staff.sendMessage("§7[${sender.name} » ${receiver.name}] $message")
		}

		if (ChatUtils.isMensagemPolemica(message)) {
			DreamNetwork.PANTUFA.sendMessageAsync(
				"387632163106848769",
				"**`" + sender.name.replace("_", "\\_") + "` escreveu uma mensagem potencialmente polêmica para `${receiver.name.replace("_", "\\_")}`!**\n```" + message + "```\n"
			)
		}

		// bossbar
		if (!isIgnoringTheSender) {
			receiver.playSound(receiver.location, "perfectdreams.sfx.msn", 1F, 1F)
			val bossBar = Bukkit.createBossBar("§b${fromName}§r§d: §d$message", BarColor.PINK, BarStyle.SOLID)

			bossBar.addPlayer(receiver)

			scheduler().schedule(DreamChat.INSTANCE) {
				while (bossBar.progress > 0) {
					val newProgress = bossBar.progress - 0.01
					if (0 >= newProgress) {
						break
					}
					bossBar.progress = newProgress
					waitFor(1)
				}

				bossBar.removeAll()
			}
		}
	}
}
