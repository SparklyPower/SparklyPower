package net.perfectdreams.pantufa.utils

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.pantufa.api.commands.styled
import java.awt.Color

object Constants {
	const val LEFT_PADDING = "\uD83D\uDD39"
	const val ERROR = "<:error:412585701054611458>"
	const val LOCAL_HOST = "127.0.0.1"
	const val PERFECTDREAMS_OFFLINE_MESSAGE = "Acho que o SparklyPower está offline..."

	val SPARKLYPOWER_OFFLINE: (suspend (UnleashedContext) -> Unit) = {
		it.reply(false) {
			styled(
				PERFECTDREAMS_OFFLINE_MESSAGE,
				ERROR
			)
		}
	}

	val LORITTA_AQUA = Color(26, 160, 254)
	val WHITE_SPACE_MULTIPLE_REGEX = Regex(" +")
	val LEFT_EMOJI = DiscordPartialEmoji(Snowflake(930922528715722782L), "chevron_left")
	val RIGHT_EMOJI = DiscordPartialEmoji(Snowflake(930922702011773038L), "chevron_right")
}