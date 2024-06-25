package net.perfectdreams.pantufa.listeners

import dev.minn.jda.ktx.interactions.components.asDisabled
import dev.minn.jda.ktx.messages.MessageEdit
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.api.commands.styled
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.Emotes
import net.perfectdreams.pantufa.utils.extensions.await
import net.sparklypower.rpc.GeneratePantufaPrintShopCustomMapsRequest
import net.sparklypower.rpc.GeneratePantufaPrintShopCustomMapsResponse
import java.util.*

class ApproveMapComponentListener(val m: PantufaBot) : ListenerAdapter() {
	private val mutex = Mutex() // Just to avoid two staff members clicking to approve the same map at the same time

	override fun onButtonInteraction(event: ButtonInteractionEvent) {
		if (event.componentId.startsWith("approve_map:")) {
			val mapId = event.componentId.substringAfterLast(":").toLong()

			m.launch {
				// Defer it ephemerally
				val deferReplyAction = event.interaction.deferReply(true).await()

				// Get the Minecraft Account of the user that clicked on the button
				val minecraftAccountId = m.getDiscordAccountFromId(event.user.idLong)
					?.minecraftId

				if (minecraftAccountId == null) {
					deferReplyAction.editOriginal(
						MessageEdit {
							styled(
								"Você precisa associar a sua conta do SparklyPower antes de poder usar este comando! Para associar, use `-registrar NomeNoServidor`!",
								Constants.ERROR
							)
						}
					).await()
					return@launch
				}

				val response = mutex.withLock {
					Json.decodeFromString<GeneratePantufaPrintShopCustomMapsResponse>(
						PantufaBot.http.post("${m.config.sparklyPower.server.sparklyPowerSurvival.apiUrl.removeSuffix("/")}/create-pantufa-print-shop-maps") {
							setBody(
								Json.encodeToString(
									GeneratePantufaPrintShopCustomMapsRequest(mapId, minecraftAccountId.toString())
								)
							)
						}.bodyAsText()
					)
				}

				when (response) {
					GeneratePantufaPrintShopCustomMapsResponse.NotEnoughPesadelos -> {
						MessageEdit {
							styled(
								"O usuário não tem pesadelos suficientes!",
								Constants.ERROR
							)
						}
					}
					GeneratePantufaPrintShopCustomMapsResponse.PluginUnavailable -> {
						deferReplyAction.editOriginal(
							MessageEdit {
								styled(
									"Plugin não está disponível, tente novamente mais tarde!",
									Constants.ERROR
								)
							}
						).await()
					}
					GeneratePantufaPrintShopCustomMapsResponse.UnknownPlayer -> {
						deferReplyAction.editOriginal(
							MessageEdit {
								styled(
									"Player desconhecido",
									Constants.ERROR
								)
							}
						).await()
					}
					GeneratePantufaPrintShopCustomMapsResponse.UnknownMapRequest -> {
						deferReplyAction.editOriginal(
							MessageEdit {
								styled(
									"Pedido de geração de mapa desconhecido",
									Constants.ERROR
								)
							}
						).await()
					}
					GeneratePantufaPrintShopCustomMapsResponse.RequestAlreadyApproved -> {
						deferReplyAction.editOriginal(
							MessageEdit {
								styled(
									"Estes mapas já foram aprovados por outra pessoa!",
									Constants.ERROR
								)
							}
						).await()
					}
					is GeneratePantufaPrintShopCustomMapsResponse.Success -> {
						event.message.editMessageComponents(
							event.message.components.asDisabled()
						).await()

						deferReplyAction.editOriginal(
							MessageEdit {
								styled(
									"Os mapas foram aprovados e o player recebeu os mapas na Caixa Postal dos Correios!",
									Emotes.PantufaComfy
								)
							}
						).await()

						// Also send a message on the user's DMs
						val requestedByMinecraftId = UUID.fromString(response.requestedById)
						val requestedByUserId = m.getDiscordAccountFromUniqueId(requestedByMinecraftId)
							?.discordId ?: return@launch // User did not connect their account to Disocrd

						try {
							val user = m.jda.retrieveUserById(requestedByUserId).await() ?: return@launch
							val imagePreviewUrl = event.message.attachments.first().url
							user.openPrivateChannel().await()
								.sendMessage("""${Emotes.PantufaThumbsUp} **A equipe aprovou a sua imagem!**
									|
									|A gráfica da Gabriela pegou **${Emotes.Pesadelos} ${response.cost} pesadelos** seus para fabricar a sua imagem em ${Emotes.FilledMap} mapas, e agora **os mapas fabricados já estão na sua Caixa Postal**!
									|
									|Pegue os mapas na `/warp correios`! (Se você estiver online no SparklyPower quando recebeu esta mensagem, você recebeu os mapas no seu inventário)
									|
									|**Sua imagem foi aprovada por:** ${event.user.asMention} (`@${event.user.name}`), que tal mandar um "obg ${event.user.asMention}" depois? ${Emotes.PantufaLick}
									|
									|$imagePreviewUrl""".trimMargin())
								.await()
						} catch (e: Exception) {
							// User does not exist!
						}
					}
				}
			}
		}
	}
}