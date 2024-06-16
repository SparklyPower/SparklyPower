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

	val ALLOWED_CHANNELS_IDS = listOf(
		378935712679985152L, // comandos SparklyPower
		830658622383980545L, // comandos SparklyPower 2
		673531793546149899L, // comandos Loritta
		704874923104927835L, // comandos Loritta 2
		798014569191571506L, // comandos Loritta 3
		798017447830880266L, // comandos Loritta 4
		417059128519819265L, // staff SparklyPower
		556589191547584544L, // staff SparklyPower cmds
		358774895850815488L, // staff Loritta
		547119872568459284L, // staff Loritta cmds
		574985687951212549L, // SparklyPower VIPs
		411606648822431744L, // loritta donators
		1251330087211630633L
	)



	val LORITTA_AQUA = Color(26, 160, 254)
	val WHITE_SPACE_MULTIPLE_REGEX = Regex(" +")
	val LEFT_EMOJI = DiscordPartialEmoji(Snowflake(930922528715722782L), "chevron_left")
	val RIGHT_EMOJI = DiscordPartialEmoji(Snowflake(930922702011773038L), "chevron_right")
}