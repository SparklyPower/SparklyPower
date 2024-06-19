package net.perfectdreams.pantufa.interactions.vanilla.minecraft

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
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
                        val survivalPages = survivalPlayers.chunked(50)
                        val lobbyPages = lobbyPlayers.chunked(50)

                        context.reply(false) {
                            embeds += buildEmbed(totalPlayersOnline, survivalPlayers, survivalPages, lobbyPlayers, lobbyPages, page)

                            actionRow(
                                context.pantufa.interactivityManager
                                    .buttonForUser(context.user, ButtonStyle.PRIMARY, builder = {
                                        this.emoji = Emoji.fromCustom(
                                            Constants.LEFT_EMOJI.name!!,
                                            Constants.LEFT_EMOJI.id!!.value.toLong(),
                                            Constants.LEFT_EMOJI.animated.discordBoolean
                                        )
                                    }) {
                                        page--

                                        it.deferAndEditOriginal {
                                            embeds += buildEmbed(totalPlayersOnline, survivalPlayers, survivalPages, lobbyPlayers, lobbyPages, page)
                                        }
                                    },
                                context.pantufa.interactivityManager
                                    .buttonForUser(context.user, ButtonStyle.PRIMARY, builder = {
                                        this.emoji = Emoji.fromCustom(
                                            Constants.RIGHT_EMOJI.name!!,
                                            Constants.RIGHT_EMOJI.id!!.value.toLong(),
                                            Constants.RIGHT_EMOJI.animated.discordBoolean
                                        )
                                    }) {
                                        page++

                                        it.deferAndEditOriginal {
                                            embeds += buildEmbed(totalPlayersOnline, survivalPlayers, survivalPages, lobbyPlayers, lobbyPages, page)
                                        }
                                    }

                            )
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
            totalPlayersOnline: Int,
            survivalPlayers: List<String>,
            survivalPages: List<List<String>>,
            lobbyPlayers: List<String>,
            lobbyPages: List<List<String>>,
            page: Int
        ) = Embed {
            title = "**Players Online no SparklyPower Network ($totalPlayersOnline players online)**"
            color = Constants.LORITTA_AQUA.rgb

            field {
                name = "SparklyPower Survival (${survivalPlayers.size})"
                value = if (survivalPlayers.isNotEmpty()) {
                    survivalPages[page].joinToString(", ", transform = { "**`$it`**" })
                } else {
                    "Ninguém online... \uD83D\uDE2D"
                }
            }

            field {
                name = "SparklyPower Lobby (${lobbyPlayers.size})"
                value = if (lobbyPlayers.isNotEmpty()) {
                    lobbyPages[page].joinToString(", ", transform = { "**`$it`**" })
                } else {
                    "Ninguém online... \uD83D\uDE2D"
                }
            }
        }
    }
}