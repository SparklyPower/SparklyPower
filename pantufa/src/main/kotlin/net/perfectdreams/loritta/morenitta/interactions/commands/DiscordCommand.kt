package net.perfectdreams.loritta.morenitta.interactions.commands

import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.interactions.commands.Command

@Serializable
data class DiscordCommand(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(command: Command) = DiscordCommand(
            command.idLong,
            command.name
        )
    }
}