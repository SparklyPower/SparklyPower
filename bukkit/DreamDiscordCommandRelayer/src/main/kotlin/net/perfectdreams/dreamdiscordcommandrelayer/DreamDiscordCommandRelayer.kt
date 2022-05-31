package net.perfectdreams.dreamdiscordcommandrelayer

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import kotlinx.coroutines.delay
import net.perfectdreams.dreamcore.utils.Databases
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.registerEvents
import net.perfectdreams.dreamdiscordcommandrelayer.dao.Command
import net.perfectdreams.dreamdiscordcommandrelayer.tables.Commands
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentLinkedQueue

class DreamDiscordCommandRelayer : KotlinPlugin(), Listener {
	val nf = DecimalFormat("##.##")
	val commandWebhook = WebhookClient.withUrl(config.getString("command-webhook")!!)
	val queue = ConcurrentLinkedQueue<String>()

	override fun softEnable() {
		super.softEnable()

		transaction(Databases.databaseNetwork) {
			SchemaUtils.createMissingTablesAndColumns(Commands)
		}

		registerEvents(this)

		launchAsyncThread {
			while (true) {
				val builder = StringBuilder()

				while (queue.isNotEmpty()) {
					val firstElement = queue.peek()

					// Current length + First Element + "\n"
					// If it will overflow, we break the loop and send the message as is
					if (builder.length + firstElement.length + 1 > 2000)
						break

					// Append the message content
					builder.append(firstElement)
					builder.append("\n")

					// And remove the current message!
					queue.remove()
				}

				if (builder.isNotEmpty()) {
					try {
						// Send the message if the content is not empty
						commandWebhook.send(
							WebhookMessageBuilder()
								.setUsername("Gabriela, a amiga dos comandos \uD83D\uDCBB")
								.setAvatarUrl("https://cdn.discordapp.com/attachments/513405772911345664/769319309977583676/gabriela_avatar.png")
								.setContent(builder.toString())
								.setAllowedMentions(AllowedMentions.none())
								.build()
						)
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				delay(1_000)
			}
		}
	}

	override fun softDisable() {
		commandWebhook.close()
	}

	@EventHandler
	fun onCommand(e: PlayerCommandPreprocessEvent) {
		queue.add("[`${e.player.location.world.name}` » `${nf.format(e.player.location.x)}`, `${nf.format(e.player.location.y)}`, `${nf.format(e.player.location.z)}`] **${e.player.name}**: `${e.message}`")

		launchAsyncThread {
			val message = e.message.split(' ')
			val command = message.first().drop(1).lowercase()

			transaction(Databases.databaseNetwork) {
				Command.new {
					player = e.player.name
					world = e.player.location.world.name
					alias = command
					args = message.drop(1).joinToString(" ").ifBlank { null }
					time = System.currentTimeMillis()
					x = e.player.location.x
					y = e.player.location.y
					z = e.player.location.z
				}
			}
		}
	}
}