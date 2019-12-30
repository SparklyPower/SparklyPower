package net.perfectdreams.dreamchat.utils.bot.responses

import com.okkero.skedule.schedule
import net.perfectdreams.dreamchat.DreamChat
import net.perfectdreams.dreamchat.utils.ChatUtils
import net.perfectdreams.dreamcore.utils.BlockUtils
import net.perfectdreams.dreamcore.utils.LocationUtils
import net.perfectdreams.dreamcore.utils.extensions.canPlaceAt
import net.perfectdreams.dreamcore.utils.scheduler
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.regex.Pattern

class PingResponse : RegExResponse() {
	init {
		patterns.add("qual".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("minha|meu".toPattern(Pattern.CASE_INSENSITIVE))
		patterns.add("ping|lat(ê|e)ncia".toPattern(Pattern.CASE_INSENSITIVE))
	}

	override fun getResponse(message: String, event: AsyncPlayerChatEvent): String? {
		val player = event.player

		return "§b" + event.player.displayName + "§a, você está com §9${player.spigot().ping}ms§a de ping!"
	}
}