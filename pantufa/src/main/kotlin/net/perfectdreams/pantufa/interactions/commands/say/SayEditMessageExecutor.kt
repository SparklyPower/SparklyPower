package net.perfectdreams.pantufa.interactions.commands.say

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.TextInputStyle
import net.perfectdreams.discordinteraktions.common.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.GuildApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.MessageCommandExecutor
import net.perfectdreams.discordinteraktions.common.entities.messages.Message
import net.perfectdreams.discordinteraktions.common.modals.components.textInput
import net.perfectdreams.pantufa.PantufaBot

class SayEditMessageExecutor(val pantufa: PantufaBot) : MessageCommandExecutor() {
    private val staffRoleId = Snowflake(332650495522897920)

    override suspend fun execute(context: ApplicationCommandContext, targetMessage: Message) {
        if (context !is GuildApplicationCommandContext || staffRoleId !in context.member.roleIds) {
            context.sendEphemeralMessage {
                content = "<:pantufa_analise:853048446813470762> **|** Você não pode usar esse comando."
            }
            return
        }

        if (targetMessage.author.id != pantufa.applicationId) {
            context.sendEphemeralMessage {
                content = "<:pantufa_analise:853048446813470762> **|** Isso não é uma mensagem minha!"
            }
        }

        context.sendModal(
            SayEditModalSubmitExecutor,
            "${targetMessage.channelId}:${targetMessage.id}",
            "Mensagem"
        ) {
            actionRow {
                textInput(SaySendModalSubmitExecutor.options.text, "Texto da Mensagem") {
                    this.placeholder = "Pantufa é muit fof"
                    this.value = targetMessage.content
                }
            }
        }
    }
}