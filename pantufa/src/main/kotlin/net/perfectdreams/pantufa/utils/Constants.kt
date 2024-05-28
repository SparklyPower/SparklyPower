package net.perfectdreams.pantufa.utils

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.commands.CommandContext
import net.perfectdreams.pantufa.interactions.commands.PantufaCommandContext
import java.awt.Color

object Constants {
	const val LEFT_PADDING = "\uD83D\uDD39"
	const val ERROR = "<:error:412585701054611458>"
	const val PERFECTDREAMS_LOBBY_PORT = 60800
	const val PERFECTDREAMS_SURVIVAL_PORT = 60800
	const val PERFECTDREAMS_BUNGEE_IP = "10.5.0.2"
	const val PERFECTDREAMS_BUNGEE_PORT = 60800
	const val LORITTA_PORT = 10699
	const val LOCAL_HOST = "127.0.0.1"
	const val PERFECTDREAMS_OFFLINE_MESSAGE = "Acho que o SparklyPower está offline..."
	const val SPARKLYPOWER_GUILD_ID = "320248230917046282"
	val SPARKLYPOWER_GUILD: Guild?
		get() = PantufaBot.INSTANCE.jda.getGuildById(SPARKLYPOWER_GUILD_ID)

	const val SPARKLYPOWER_STAFF_CHANNEL_ID = 417059128519819265L

	val PERFECTDREAMS_OFFLINE: ((CommandContext) -> Unit) = { context ->
		context.reply(
				PantufaReply(
						content = Constants.PERFECTDREAMS_OFFLINE_MESSAGE,
						prefix = Constants.ERROR
				)
		)
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
		411606648822431744L // loritta donators
	)

	val SPARKLYPOWER_OFFLINE: (suspend (PantufaCommandContext) -> Unit) = { context ->
		context.reply(
			PantufaReply(
				content = Constants.PERFECTDREAMS_OFFLINE_MESSAGE,
				prefix = Constants.ERROR
			)
		)
	}

	val LORITTA_AQUA = Color(26, 160, 254)

	val WHITE_SPACE_MULTIPLE_REGEX = Regex(" +")

	val LEFT_EMOJI = DiscordPartialEmoji(Snowflake(930922528715722782L), "chevron_left")

	val RIGHT_EMOJI = DiscordPartialEmoji(Snowflake(930922702011773038L), "chevron_right")
}