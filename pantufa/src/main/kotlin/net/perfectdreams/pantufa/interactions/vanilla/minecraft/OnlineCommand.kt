package net.perfectdreams.pantufa.interactions.vanilla.minecraft

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.pantufa.utils.Constants.SPARKLYPOWER_OFFLINE
import net.perfectdreams.pantufa.api.commands.PantufaReply
import net.perfectdreams.pantufa.utils.socket.SocketUtils
import net.perfectdreams.pantufa.api.commands.styled

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
                    val replies = mutableListOf<PantufaReply>()
                    val totalPlayersOnline = servers.sumOf { it["players"].array.size() }

                    replies.add(
                        PantufaReply(
                            content = "**Players Online no SparklyPower Network ($totalPlayersOnline players online)**",
                            prefix = Emotes.PantufaPickaxe.asMention
                        )
                    )

                    servers.forEach { jArr ->
                        val obj = jArr.obj
                        val name = obj["name"].string
                        val players = obj["players"].array.map {
                            it["name"].string
                        }.sorted()

                        val fancyName = prettyNameServer[name]

                        if (fancyName != null) {
                            if (players.isNotEmpty()) {
                                replies.add(
                                    PantufaReply(
                                        "**$fancyName (${players.size})**: ${players.joinToString(", ", transform = { "**`$it`**" })}",
                                        mentionUser = false
                                    )
                                )
                            } else {
                                replies.add(
                                    PantufaReply(
                                        "**$fancyName (0)**: Ninguém online... \uD83D\uDE2D",
                                        mentionUser = false
                                    )
                                )
                            }
                        }
                    }

                    context.pantufa.launch {
                        val playerOnlineGraphImage = if (showGraph)
                            context.pantufa.playersOnlineGraph.getCachedGraph().inputStream()
                        else null

                        context.reply(false) {
                            for (reply in replies) {
                                styled(reply)
                            }

                            if (playerOnlineGraphImage != null) {
                                files += FileUpload.fromData(playerOnlineGraphImage, "graph.png")
                            }
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
    }
}