package net.perfectdreams.pantufa.interactions.commands

import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.dao.DiscordAccount
import net.perfectdreams.pantufa.utils.PantufaReply

object MinecraftUserDisplayUtils {
    suspend fun replyWithAccountInformation(
        pantufa: PantufaBot,
        context: PantufaCommandContext,
        discordAccount: DiscordAccount?,
        userInfo: net.perfectdreams.pantufa.dao.User?,
    ) {
        val minecraftId = discordAccount?.minecraftId

        val survivalOnlineTrackedHours = minecraftId?.let { pantufa.getPlayerTimeOnlineInTheLastXDays(it, 30) }

        val replies = mutableListOf(
            PantufaReply(
                "**Informações da conta de <@${discordAccount?.discordId}>**"
            ),
            PantufaReply(
                "**Nome:** `${userInfo?.username}`",
                mentionUser = false
            ),
            PantufaReply(
                "**UUID:** `${userInfo?.id}`",
                mentionUser = false
            ),
            PantufaReply(
                "**A conta já foi conectada?** ${discordAccount?.isConnected}",
                mentionUser = false
            ),
        )

        if (survivalOnlineTrackedHours != null) {
            val input = survivalOnlineTrackedHours.duration.seconds
            val numberOfDays = input / 86400
            val numberOfHours = input % 86400 / 3600
            val numberOfMinutes = input % 86400 % 3600 / 60

            val niceFormattedTime = "$numberOfDays dias, $numberOfHours horas e $numberOfMinutes minutos"

            replies.add(
                PantufaReply(
                    "**Tempo Online no SparklyPower Survival nos Últimos 30 Dias (desde ${survivalOnlineTrackedHours.since.toInstant().toKotlinInstant().toMessageFormat(DiscordTimestampStyle.ShortDate)}):** $niceFormattedTime",
                    mentionUser = false
                )
            )
        }

        context.reply(*replies.toTypedArray())
    }
}