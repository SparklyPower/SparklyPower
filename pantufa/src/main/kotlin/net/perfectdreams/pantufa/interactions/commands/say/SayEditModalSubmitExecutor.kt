package net.perfectdreams.pantufa.interactions.commands.say

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.TextInputStyle
import net.perfectdreams.discordinteraktions.common.modals.*
import net.perfectdreams.discordinteraktions.common.modals.components.ModalArguments
import net.perfectdreams.discordinteraktions.common.modals.components.ModalComponents
import net.perfectdreams.pantufa.PantufaBot

class SayEditModalSubmitExecutor(val m: PantufaBot) : ModalExecutor {
    companion object : ModalExecutorDeclaration("say_edit") {
        object Options : ModalComponents() {
            val text = textInput("text", TextInputStyle.Paragraph)
        }

        override val options = Options
    }

    override suspend fun onSubmit(context: ModalContext, args: ModalArguments) {
        val (channelIdAsString, messageIdAsString) = context.data.split(":")

        m.rest.channel.editMessage(
            Snowflake(channelIdAsString),
            Snowflake(messageIdAsString)
        ) {
            content = args[options.text]
        }

        context.sendEphemeralMessage {
            content = "Mensagem Editada!"
        }
    }
}