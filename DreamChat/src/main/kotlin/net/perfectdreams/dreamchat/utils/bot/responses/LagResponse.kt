package net.perfectdreams.dreamchat.utils.bot.responses

import com.okkero.skedule.schedule
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.utils.ChatUtils
import net.perfectdreams.dreamcore.utils.BlockUtils
import net.perfectdreams.dreamcore.utils.LocationUtils
import net.perfectdreams.dreamcore.utils.extensions.canPlaceAt
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.regex.Pattern

class LagResponse : RegExResponse() {
	init {
		patterns.add("server|servidor|sparklypower".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("lag|travad".toPattern(Pattern.CASE_INSENSITIVE))
	}

	override fun getResponse(message: String, event: AsyncPlayerChatEvent): String? {
		val tps = Bukkit.getTPS()
		val tpsNow = tps[0]

		val player = event.player
		val ping = player.spigot().ping

		// kk sou muito engraçado
		if (message.contains("pingulin", true) || message.contains("pingolin", true) || message.contains("pingola", true))
			return "§b" + event.player.displayName + "§a, consiga uma régua microscópia que aí a gente descobre o tamanho do seu!"

		if (tpsNow >= 18) { // Não está lagado
			return when {
				ping >= 400 -> "§b" + event.player.displayName + "§a, o servidor não está travado... mas sabe o que está travado? A sua internet. (§9${ping}ms§a)"
				else -> "§b" + event.player.displayName + "§a, o servidor não está travado, verifique a sua conexão da sua internet! ʕ•ᴥ•ʔ §a(§9$tpsNow TPS§a)"
			}
		}

		if (tpsNow >= 16) { // 1/4 lagado
			return "§b" + event.player.displayName + "§a, o servidor está meeeeio travadinho... fique calmo, respire fundo e torça para que pare de travar! §a(§9$tpsNow TPS§a)"
		}

		if (tpsNow >= 14) {
			return "§b" + event.player.displayName + "§a, o servidor está travadinho... calma, não entre em pânico! Por favor, não ofenda o servidor... §a(§9$tpsNow TPS§a)"
		}

		return "§b" + event.player.displayName + "§a, o servidor está §c§ltravado§a! Acho que alguém tropeçou no fio, né? Fique calmo e ajude o servidor dando os seus (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧ poderes ✧ﾟ･: *ヽ(◕ヮ◕ヽ) §a(§9$tpsNow TPS§a)"
	}
}