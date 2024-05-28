package net.perfectdreams.pantufa.listeners

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.DiscordInteraction
import dev.kord.common.entity.InteractionType
import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dv8tion.jda.api.events.RawGatewayEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.discordinteraktions.common.DiscordInteraKTions
import net.perfectdreams.discordinteraktions.common.requests.InteractionRequestState
import net.perfectdreams.discordinteraktions.common.requests.RequestBridge
import net.perfectdreams.discordinteraktions.common.requests.managers.InitialHttpRequestManager
import net.perfectdreams.discordinteraktions.common.utils.Observable

@OptIn(KordPreview::class)
class InteractionListener(
    val rest: RestClient,
    val applicationId: Snowflake,
    val interaKTions: DiscordInteraKTions
) : ListenerAdapter() {
    companion object {
        private val json = Json {
            // If there're any unknown keys, we'll ignore them instead of throwing an exception.
            this.ignoreUnknownKeys = true
        }

        private val logger = KotlinLogging.logger {}
    }

    override fun onRawGateway(event: RawGatewayEvent) {
        // Workaround for Discord InteraKTions!
        if (event.type != "INTERACTION_CREATE")
            return

        // From "GatewayKordInteractions.kt"
        val interactionsEvent = event.payload.toString()

        // Kord still has some fields missing (like "deaf") so we need to decode ignoring missing fields
        val request = json.decodeFromString<DiscordInteraction>(interactionsEvent)

        val observableState = Observable(InteractionRequestState.NOT_REPLIED_YET)
        val bridge = RequestBridge(observableState)

        val requestManager = InitialHttpRequestManager(
            bridge,
            interaKTions.kord,
            applicationId,
            request.id,
            request.token,
        )

        bridge.manager = requestManager

        if (request.type == InteractionType.ApplicationCommand)
            interaKTions.commandChecker.checkAndExecute(
                request,
                requestManager
            )
        else if (request.type == InteractionType.Component) {
            interaKTions.componentChecker.checkAndExecute(
                request,
                requestManager
            )
        } else if (request.type == InteractionType.ModalSubmit) {
            interaKTions.modalChecker.checkAndExecute(
                request,
                requestManager
            )
        }
    }
}