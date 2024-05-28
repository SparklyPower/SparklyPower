package net.perfectdreams.pantufa.utils

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.tables.VotesUserAvailableNotifications
import net.perfectdreams.pantufa.utils.extensions.await
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ServerVotesNotifier(val m: PantufaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun run() {
        val now = Instant.now()

        m.transactionOnSparklyPowerDatabase {
            // Get all users that needs to be notified
            val usersToBeNotifiedData = VotesUserAvailableNotifications.select {
                VotesUserAvailableNotifications.notified eq false and (VotesUserAvailableNotifications.notifyAt lessEq now)
            }.toList()

            // Notify them!
            for (userToBeNotifiedData in usersToBeNotifiedData) {
                val userId = userToBeNotifiedData[VotesUserAvailableNotifications.userId]

                try {
                    val discordAccount = m.getDiscordAccountFromUniqueId(userId) ?: continue
                    val user = runBlocking { m.jda.retrieveUserById(discordAccount.discordId).await() }

                    if (user != null) {
                        logger.info { "Notifying user ${user.idLong} about top.gg vote..." }
                        runBlocking {
                            user.openPrivateChannel().await()
                                .sendMessageEmbeds(
                                    EmbedBuilder()
                                        .setColor(Constants.LORITTA_AQUA)
                                        .setThumbnail("https://assets.perfectdreams.media/sparklypower/pantufa-happy.gif")
                                        .setTitle("Vote no SparklyPower no ${userToBeNotifiedData[VotesUserAvailableNotifications.serviceName]}! <:pantufa_yay:1004450173495287848>")
                                        .setDescription(
                                            """Ei, você já pode votar no SparklyPower no site ${userToBeNotifiedData[VotesUserAvailableNotifications.serviceName]}! <:pantufa_lurk:1012841674856214528>
                                            |
                                            |Você pode votar todos os dias, deixando você votar novamente após às <t:1490850000:t>, e votando você ajuda o SparklyPower a crescer e, é claro, você ganha recompensas! <:pantufa_heart:853048447175098388>
                                            |
                                            |https://sparklypower.net/votar""".trimMargin()
                                        )
                                        .build()
                                )
                                .await()
                        }
                    }
                } catch (e: Exception) {}
            }

            VotesUserAvailableNotifications.update({ VotesUserAvailableNotifications.id inList usersToBeNotifiedData.map { it[VotesUserAvailableNotifications.id] }}) {
                it[VotesUserAvailableNotifications.notified] = true
            }
        }
    }
}