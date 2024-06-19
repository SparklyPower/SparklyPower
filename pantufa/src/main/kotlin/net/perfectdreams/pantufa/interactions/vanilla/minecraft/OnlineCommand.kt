package net.perfectdreams.pantufa.interactions.vanilla.minecraft

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.utils.Constants.SPARKLYPOWER_OFFLINE
import net.perfectdreams.pantufa.utils.socket.SocketUtils
import net.perfectdreams.pantufa.utils.Constants

class OnlineCommand : SlashCommandDeclarationWrapper {
    companion object {
        val prettyNameServer = hashMapOf(
            "sparklypower_lobby" to "SparklyPower Lobby",
            "sparklypower_survival" to "SparklyPower Survival",
        )
    }

    override fun command() = slashCommand("online", "Veja os players que estão online no SparklyPower!", CommandCategory.MINECRAFT) {
        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("online")
        }

        executor = OnlineCommandExecutor()
    }

    inner class OnlineCommandExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val showGraph = false // args[options.showGraph]
            val jsonObject = JsonObject()
            val sparklyPower = context.pantufa.config.sparklyPower
            jsonObject["type"] = "getOnlinePlayersInfo"

            SocketUtils.sendAsync(
                jsonObject,
                host = sparklyPower.server.perfectDreamsBungeeIp,
                port = sparklyPower.server.perfectDreamsBungeePort,
                success = { response ->
                    val servers = response["servers"].array
                    val totalPlayersOnline = servers.sumOf { it["players"].array.size() }
                    val survivalPlayers = mutableListOf<String>()
                    val lobbyPlayers = mutableListOf<String>()
                    var page = 0

                    servers.forEach { jArr ->
                        val obj = jArr.obj
                        val name = obj["name"].string
                        val players = obj["players"].array.map {
                            it["name"].string
                        }.sorted()

                        when (name) {
                            "sparklypower_survival" -> {
                                survivalPlayers.addAll(players)
                            }
                            "sparklypower_lobby" -> {
                                lobbyPlayers.addAll(players)
                            }
                        }
                    }

                    context.pantufa.launch {
                        context.reply(false) {
                            embeds += buildEmbed("SparklyPower Survival", survivalPlayers)
                            embeds += buildEmbed("SparklyPower Lobby", lobbyPlayers)
                        }
                    }
                }, error = {
                    context.pantufa.launch {
                        SPARKLYPOWER_OFFLINE.invoke(context)
                    }
                }
            )
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }

        private fun buildEmbed(
            sectionName: String,
            sectionPlayers: List<String>
        ) = Embed {
            title = "**Players Online no $sectionName (${sectionPlayers.size} players online)**"
            color = Constants.LORITTA_AQUA.rgb

            description = if (sectionPlayers.isNotEmpty()) {
                sectionPlayers.joinToString(", ", transform = { "**`$it`**" })
            } else "Ninguém online... \uD83D\uDE2D"
        }
    }
}